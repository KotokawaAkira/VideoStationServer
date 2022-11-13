package com.lingyi.RootGet.controller;

import com.lingyi.RootGet.entry.Account;
import com.lingyi.RootGet.entry.Video;
import com.lingyi.RootGet.mapper.video.AccountMapper;
import com.lingyi.RootGet.mapper.video.CommentMapper;
import com.lingyi.RootGet.mapper.video.VideoMapper;
import com.lingyi.RootGet.tools.Constant;
import com.lingyi.RootGet.tools.MD5Transfer;
import com.lingyi.RootGet.tools.RedisTools;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final RedisTools redisTools;
    private final AccountMapper accountMapper;
    private final VideoMapper videoMapper;
    private final CommentMapper commentMapper;

    public AccountController(RedisTools redisTools, AccountMapper accountMapper, VideoMapper videoMapper,CommentMapper commentMapper) {
        this.redisTools = redisTools;
        this.accountMapper = accountMapper;
        this.videoMapper = videoMapper;
        this.commentMapper = commentMapper;
    }

    @PostMapping(value = "/login")
    public String login(@RequestBody Map<String, String> body, HttpServletResponse response) {
        //是否输入账号密码
        String id = body.get("id");
        String pwd = body.get("password");
        if (!StringUtils.hasText(id) || !StringUtils.hasText(pwd)) {
            //response.setStatus(401);
            return Constant.FAILURE;
        }
        try {
            Long.parseLong(id);
        } catch (Exception e) {
            return Constant.FAILURE;
        }
        Account account = redisTools.selectAccountInCache(Long.parseLong(id));
        //账号被封禁
        if(account !=null && account.getBanned() == 1)
            return Constant.AccountBanned;
        //验证账号密码
        if (account != null && MD5Transfer.encode(pwd).equals(account.getPassword())) {
            //添加cookie
            Cookie cookie_uid = new Cookie("uid", id);
            cookie_uid.setPath("/");
            cookie_uid.setMaxAge(3600 * 24 * 7);
            String token = redisTools.setAccountToken(account.getId(), 7, TimeUnit.DAYS);
            Cookie cookie_token = new Cookie("token", token);
            cookie_token.setPath("/");
            cookie_token.setMaxAge(3600 * 24 * 7);
            response.addCookie(cookie_uid);
            response.addCookie(cookie_token);
        } else return Constant.FAILURE;
        return Constant.SUCCESS;
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String id_session = request.getSession().getId();
        boolean lock = redisTools.setLock(id_session, "Register");

        if (!lock) {
            long ttl = redisTools.getLockTTL(id_session, "Register");
            return "{\"success\":\"failure\",\"reason\":\"TooMuchTimes\",\"wait\":\"" + ttl + "\"}";
        }
        String name = body.get("name");
        String password = body.get("password");
        Account account = new Account();
        account.setName(name).setPassword(MD5Transfer.encode(password));
        accountMapper.addOne(account);

        return "{\"id\":\"" + account.getId() + "\",\"name\":\"" + account.getName() + "\",\"password\":\"******\"}";
    }

    @GetMapping("/selectOneById/{id}")
    public Account selectOne(@PathVariable("id") long id) {
        return redisTools.selectAccountInCache(id).setPassword("******");
    }

    @GetMapping("/userSearch")
    public List<Account> search(@RequestParam("keywords") String keywords) {
        if (!StringUtils.hasText(keywords)) return new ArrayList<>();
        List<Account> accounts = accountMapper.selectByKeywords(keywords);
        for (Account account : accounts) {
            account.setPassword("******");
        }
        return accounts;
    }

    @GetMapping("/getLoginInfo")
    public String getLoginInfo(HttpServletRequest request) {
        String uid = Constant.getUidInCookie(request);
        String token = Constant.getTokenInCookie(request);
        if (uid == null || token == null) return Constant.FAILURE;
        String accountToken = redisTools.getAccountToken(Long.parseLong(uid));
        if (!token.equals(accountToken)) return Constant.FAILURE;
        return "{\"success\":\"true\",\"uid\":\"" + uid + "\"}";
    }

    @PostMapping("/upLoadProfile/{uid}")
    public String upload(@PathVariable("uid") String uid, @RequestParam("file") MultipartFile multipartFile) {
        File root = new File("/opt/Server/videoStation/profile/" + uid);
        if (!root.exists()) root.mkdirs();
        File profile = new File(root.getAbsolutePath() + "/profile.file");
        if (profile.exists())
            profile.delete();
        try {
            multipartFile.transferTo(profile);
        } catch (IOException e) {
            throw new RuntimeException(e);
            //return Constant.FAILURE;
        }
        return Constant.SUCCESS;
    }

    @GetMapping("/getProfile/{uid}")
    public void profile(@PathVariable("uid") String uid, HttpServletResponse response) {
        File profile = new File("/opt/Server/videoStation/profile/" + uid + "/profile.file");
        if (!profile.exists()) {
            response.setStatus(404);
            return;
        }
        response.setContentType("image/png");
        ServletOutputStream outputStream;
        BufferedInputStream bis = null;
        try {
            outputStream = response.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(profile));
            int len;
            byte[] bytes = new byte[2048];
            while ((len = bis.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            bis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                bis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @GetMapping("/logout/{uid}")
    public void logout(@PathVariable("uid") long uid) {
        redisTools.getRedisTemplate().delete("VideoStation:Token:" + uid);
    }

    @PostMapping("/change")
    public String changeName(@RequestBody Map<String, String> body, HttpServletRequest request) {
        //获取参数
        String uid = body.get("uid");
        long uid_long = Long.parseLong(uid);
        //权限认证
        if (!isLogin(uid_long, request))
            return Constant.NoPermission;
        //获取账号
        Account account = redisTools.selectAccountInCache(uid_long);
        if (account == null) return Constant.AccountNotExists;
        String newName = body.get("newName");
        String newPassword = body.get("newPassword");
        String password = body.get(("password"));
        if (StringUtils.hasText(newName))
            account.setName(newName);
        if (StringUtils.hasText(newPassword)&&StringUtils.hasText(password)){
            if(!MD5Transfer.encode(password).equals(account.getPassword()))
                return Constant.PasswordIncorrect;
            account.setPassword(MD5Transfer.encode(newPassword));
        }
        //更改数据
        accountMapper.updateOne(account);
        redisTools.getRedisTemplate().opsForHash().put("VideoStation:Account:" + uid, "name", account.getName());
        redisTools.getRedisTemplate().opsForHash().put("VideoStation:Account:" + uid, "password", account.getPassword());

        List<Video> list = videoMapper.selectByUp(uid_long);
        if(list!=null&&!list.isEmpty()){
            commentMapper.updateName(list.get(0).getUpName(),newName);
            videoMapper.updateUpName(list.get(0).getUpName(),newName);
            for (Video video : list) {
                video.setUpName(newName);
                redisTools.getRedisTemplate().opsForHash().put("VideoStation:Video:" + video.getId(), "upName", newName);
            }
        }

        return Constant.SUCCESS;
    }

    private boolean isLogin(long id, HttpServletRequest request) {
        String uid = Constant.getUidInCookie(request);
        String token = Constant.getTokenInCookie(request);
        if (!StringUtils.hasText(uid) || !StringUtils.hasText(token))
            return false;
        long uid_long = Long.parseLong(uid);
        if (uid_long != id)
            return false;
        String accountToken = redisTools.getAccountToken(id);
        if (!token.equals(accountToken))
            return false;
        return true;
    }
}
