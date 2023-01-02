package com.lingyi.RootGet.controller;

import com.lingyi.RootGet.entry.Account;
import com.lingyi.RootGet.entry.Video;
import com.lingyi.RootGet.mapper.video.AccountMapper;
import com.lingyi.RootGet.mapper.video.VideoMapper;
import com.lingyi.RootGet.tools.Constant;
import com.lingyi.RootGet.tools.MD5Transfer;
import com.lingyi.RootGet.tools.RedisTools;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class Administrator {
    private final RedisTools redisTools;
    private final VideoMapper videoMapper;
    private final AccountMapper accountMapper;

    public Administrator(RedisTools redisTools, VideoMapper videoMapper, AccountMapper accountMapper) {
        this.redisTools = redisTools;
        this.videoMapper = videoMapper;
        this.accountMapper = accountMapper;
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String id = body.get("id");
        long id_long;
        String password = body.get("password");
        if (!StringUtils.hasText(id) || !StringUtils.hasText(password))
            return Constant.NoPermission;
        try {
            id_long = Long.parseLong(id);
        } catch (Exception e) {
            return Constant.FAILURE;
        }
        if (id_long != 10063301)
            return Constant.NoPermission;
        Account account = redisTools.selectAccountInCache(id_long);
        if (!account.getPassword().equals(MD5Transfer.encode(password)))
            return Constant.PasswordIncorrect;
        HttpSession session = request.getSession();
        session.setAttribute("id", id);
        return Constant.SUCCESS;
    }

    @GetMapping("/getLoginInfo")
    public String getLoginInfo(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String id = (String) session.getAttribute("id");
        if (id == null)
            return Constant.FAILURE;
        return "{\"success\":\"true\",\"id\":\"" + id + "\"}";
    }

    @GetMapping("/delete/video/{id}")
    public String deleteVideo(@PathVariable("id") long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Constant.NoPermission;
        Video video = redisTools.getFromHashAsObject("VideoStation:Video:" + id, Video.class);
        if (video == null) return Constant.FAILURE;
        redisTools.getRedisTemplate().delete("VideoStation:Video:" + id);
        videoMapper.deleteOne(String.valueOf(id));
        File videoImg = new File("/opt/Server/videoStation/videoImg/" + id);
        if (videoImg.exists()) videoImg.delete();
        File videoFile = new File("/opt/Server/videoStation/video/" + id);
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

    @GetMapping("/getAllAccount")
    public List<Account> getAll(HttpServletRequest request) {
        if (!isAdmin(request))
            return new ArrayList<>();
        return accountMapper.getAll();
    }

    @PostMapping("/update/{id}")
    public String ban(@PathVariable("id") long id,@RequestBody Map<String,String> body,  HttpServletRequest request) {
        String name = body.get("name");
        String banned = body.get("banned");
        String password = body.get("password");
        if (!isAdmin(request))
            return Constant.NoPermission;
        Account account = redisTools.selectAccountInCache(id);
        if (account == null) return Constant.FAILURE;
        if(banned!=null){
            account.setBanned(Integer.parseInt(banned));
            redisTools.getRedisTemplate().opsForHash().put("VideoStation:Account:" + account.getId(),"banned",banned);
        }
        if(name!=null){
            account.setName(name);
            redisTools.getRedisTemplate().opsForHash().put("VideoStation:Account:" + account.getId(),"name",name);
        }
        if(password!=null){
            String newPwd = MD5Transfer.encode(password);
            account.setPassword(newPwd);
            redisTools.getRedisTemplate().opsForHash().put("VideoStation:Account:" + account.getId(),"password",newPwd);
        }
        accountMapper.updateOne(account);
        return Constant.SUCCESS;
    }
    @PostMapping("/updateVideo/{id}")
    public String updateVideo(@PathVariable("id")String id,@RequestBody Map<String,String> body,HttpServletRequest request){
        String title = body.get("title");
        String summary = body.get("summary");
        if (!isAdmin(request))
            return Constant.NoPermission;
        Video video = redisTools.selectVideoInCache(id);
        if (video==null) return Constant.FAILURE;
        if(title!=null){
            video.setTitle(title);
            redisTools.getRedisTemplate().opsForHash().put("VideoStation:Video:"+id,"title",title);
        }
        if(summary!=null){
            video.setTitle(title);
            redisTools.getRedisTemplate().opsForHash().put("VideoStation:Video:"+id,"summary",summary);
        }
        return Constant.SUCCESS;
    }
    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String id = (String) session.getAttribute("id");
        if (id == null || !id.equals("10063301")) return false;
        return true;
    }
}
