package com.lingyi.RootGet.controller;

import com.lingyi.RootGet.entry.Account;
import com.lingyi.RootGet.entry.Video;
import com.lingyi.RootGet.mapper.web.InfoMapper;
import com.lingyi.RootGet.mapper.web.WordsMapper;
import com.lingyi.RootGet.entry.Info;
import com.lingyi.RootGet.entry.Words;

import com.lingyi.RootGet.tools.RedisTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 这个是类与此项目无关 up自用的，懒得删
 **/
@Controller
public class Viewer {
    private final InfoMapper infoMapper;
    private final WordsMapper wordsMapper;
    private final RedisTools redisTools;

    @Autowired
    public Viewer(InfoMapper infoMapper, WordsMapper wordsMapper, RedisTools redisTools) {
        this.infoMapper = infoMapper;
        this.wordsMapper = wordsMapper;
        this.redisTools = redisTools;
    }

    @RequestMapping("/getWebs")
    @ResponseBody
    public List getWebs() {
        List<Info> info = infoMapper.getInfo();
        return info;
    }

    @RequestMapping("/getWords")
    @ResponseBody
    public List<Words> getWords() {
        return wordsMapper.getWords();
    }

    @RequestMapping("/Video/{id}")
    public String getVideo(@PathVariable("id") String id) {
        Video video = redisTools.selectVideoInCache(id);
        if (video == null) return null;
            redisTools.addOnePlay(id);
        return "/video/index.html";
    }

    @RequestMapping("/Mine/{id}")
    public String mine(@PathVariable("id") String id) {
        long uid = Long.parseLong(id);
        Account account = redisTools.selectAccountInCache(uid);
        if(account==null) return null;
        return "/mine/index.html";
    }
}
