package com.lingyi.RootGet.controller;

import com.lingyi.RootGet.entry.Account;
import com.lingyi.RootGet.entry.Video;
import com.lingyi.RootGet.mapper.video.VideoMapper;
import com.lingyi.RootGet.tools.Constant;
import com.lingyi.RootGet.tools.RedisTools;
import com.lingyi.RootGet.tools.SnowFlake;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class VideoController {
    private final RedisTools redisTools;
    private final VideoMapper videoMapper;

    @Autowired
    private VideoController(RedisTools redisTools, VideoMapper videoMapper) {
        this.redisTools = redisTools;
        this.videoMapper = videoMapper;
    }
    //获取视频列表
    @GetMapping("/video/videoList/{id}")
    public List<String> getVideoList(@PathVariable("id") String id) {
        StringRedisTemplate redisTemplate = redisTools.getRedisTemplate();
        Boolean hasKey = redisTemplate.hasKey("VideoStation:Video:" + id);
        if (!hasKey) return null;
        File root = new File("视频保存文件夹" + id);
        File[] files = root.listFiles();
        if (files == null || files.length == 0) return null;
        List<String> fileList = new ArrayList<>();
        for (File file : files) {
            fileList.add(file.getName());
        }
        return fileList;
    }
    //获取视频文件
    @GetMapping("/video/{id}/{filename}")
    public String getVideo(@PathVariable("id") long id, @PathVariable("filename") String filename, HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Sec-Fetch-Site");
        String userAgent = request.getHeader("User-Agent");
        UserAgent ua = UserAgent.parseUserAgentString(userAgent);
        Browser browser = ua.getBrowser().getGroup();
        String bwt = browser.toString();
        if(origin!=null){
            if (!origin.equals("same-origin")) {
                response.setStatus(403);
                return null;
            }
        }
        //文件位置
        File file = new File("视频保存文件夹" + id + "/" + filename);
        if (!file.exists()) {
            return Constant.FileNotExists;
        }
        //初始化开始和结束
        long start = 0;
        long end = file.length() - 1;
        //从HttpRequestHeader中获取Range(文件断点开始处)
        String range = request.getHeader("Range");
        //若没有断点，则直接完整下载
        if (range != null) {
            //存在断点，重新设置开始和结束
            String[] strings = range.split("=");
            String[] split = strings[1].split("-");
            start = Long.parseLong(split[0]);
            if (split.length > 1) end = Long.parseLong(split[1]);
        }
        response.setContentType("video/" + Constant.getFileType(file));
        if (start == 0) {
            //response.setContentLengthLong(file.length());
            response.setStatus(200);
        } else {
            response.setStatus(206);
        }
        response.setContentLengthLong(end - start + 1);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + file.length());
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            //跳过的文件长度等于断点位置，可将BufferedInputStream更换为RandomAccessFile
            bis.skip(start);
            byte[] bytes = new byte[2048];
            int len;
            while ((len = bis.read(bytes)) != -1) {
                response.getOutputStream().write(bytes, 0, len);
            }
        } catch (IOException e) {
            return "{\"success\":\"false\"}";
            //e.printStackTrace();
        }finally {
            try {
                bis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Constant.SUCCESS;
    }
    //获取视频信息
    @GetMapping("/video/information/{id}")
    public Video getInformation(@PathVariable("id") String id) {
        return redisTools.selectVideoInCache(id);
    }
    //获取首页获取视频
    @GetMapping("/video/getVideos/{count}")
    public Set<Video> getVideos(@PathVariable("count") int count, @RequestParam("first") boolean isFirst, HttpServletRequest request) {
        return redisTools.getVideos(count, request.getSession(), isFirst);
    }
    //搜索
    @GetMapping("/video/search")
    public List<Video> search(@RequestParam("keywords") String keywords) {
        List<Video> list = new ArrayList<>();
        if (!StringUtils.hasText(keywords)) return list;
        Set<String> keys = redisTools.getKeys("VideoStation:Video:*");
        for (String key : keys) {
            Video video = redisTools.getFromHashAsObject(key, Video.class);
            if (video.getTitle().toLowerCase().contains(keywords.toLowerCase()))
                list.add(video);
        }
        return rankByPlay(list);
    }
    //点赞
    @GetMapping("/video/addLike/{id}")
    public long addLike(@PathVariable("id") String id) {
        if (!redisTools.getRedisTemplate().hasKey("VideoStation:Video:" + id)) return -1;
        return redisTools.videoAddALike(id);
    }
    //取消点赞
    @GetMapping("/video/disLike/{id}")
    public long disLike(@PathVariable("id") String id) {
        if (!redisTools.getRedisTemplate().hasKey("VideoStation:Video:" + id)) return -1;
        return redisTools.videoAddADisLike(id);
    }
    //查询用户上传
    @GetMapping("/video/getByUp/{uid}")
    public List<Video> getByUp(@PathVariable("uid") long uid) {
        return videoMapper.selectByUp(uid);
    }
    //上传视频
    @PostMapping("/video/addVideo")
    public String addNew(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        String summary = body.get("summary");
        String up = body.get("up");
        Account account = redisTools.selectAccountInCache(Long.parseLong(up));
        ArrayList<String> forbiddenWords = Constant.getForbiddenWords();
        if (forbiddenWords.contains(title) || forbiddenWords.contains(summary)) return Constant.WordsForbidden;
        SnowFlake snowFlake = new SnowFlake(1);
        long id = snowFlake.nextId();
        Video video = new Video();
        video.setId(String.valueOf(id));
        video.setTitle(title);
        video.setUp(Long.parseLong(up));
        video.setSummary(summary);
        video.setUpName(account.getName());
        videoMapper.addOne(video);
        video = videoMapper.selectOneById(String.valueOf(id));
        redisTools.setAsHash("VideoStation:Video:" + id, video, -1, null);
        return "{\"success\":\"true\",\"id\":\"" + video.getId() + "\"}";
    }

    @PostMapping("/video/uploadVideoImg/{id}")
    public String uploadVideoImg(@PathVariable("id") long id, @RequestPart("videoImg") MultipartFile multipartFile) {
        File videoImg = new File("视频封面文件夹" + id);
        if (!videoImg.getParentFile().exists()) videoImg.getParentFile().mkdirs();
        if (videoImg.exists()) videoImg.delete();
        try {
            multipartFile.transferTo(videoImg);
            return Constant.SUCCESS;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //上传视频文件保存
    @PostMapping("/video/uploadVideo/{id}")
    public String uploadVideo(@PathVariable("id") long id, @RequestPart("video") MultipartFile multipartFile) {
        File videoFile = new File("视频封面文件夹" + id + "/" + multipartFile.getOriginalFilename());
        if (!videoFile.getParentFile().exists()) videoFile.getParentFile().mkdirs();
        if (videoFile.exists()) videoFile.delete();
        try {
            multipartFile.transferTo(videoFile);
            return Constant.SUCCESS;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //删除视频
    @PostMapping("/video/delete")
    public String delete(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        String uid = body.get("uid");

        Video video = redisTools.getFromHashAsObject("VideoStation:Video:" + id, Video.class);
        if (video == null) return Constant.FAILURE;
        if (video.getUp() != Long.parseLong(uid)) return Constant.NoPermission;
        redisTools.getRedisTemplate().delete("VideoStation:Video:" + id);
        videoMapper.deleteOne(id);
        File videoImg = new File("视频封面文件夹" + id);
        if (videoImg.exists()) videoImg.delete();
        File videoFile = new File("视频文件夹" + id);
        if (videoFile.exists()) {
            File[] files = videoFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            videoFile.delete();
        }
        return Constant.SUCCESS;
    }
    //排序
    private List<Video> rankByPlay(List<Video> arr) {
        if (arr != null && arr.size() > 1) {
            for (int i = 0; i < arr.size() - 1; i++) {
                // 初始化一个布尔值
                boolean flag = true;
                for (int j = 0; j < arr.size() - i - 1; j++) {
                    if (arr.get(j).getPlay() > arr.get(j + 1).getPlay()) {
                        // 调换
                        Video temp;
                        temp = arr.get(j);
                        arr.set(j, arr.get(j + 1));
                        arr.set(j + 1, temp);
                        // 改变flag
                        flag = false;
                    }
                }
                if (flag) {
                    break;
                }
            }
            Collections.reverse(arr);
        }
        System.out.println(arr);
        return arr;
    }
}
