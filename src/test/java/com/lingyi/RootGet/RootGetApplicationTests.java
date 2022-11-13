package com.lingyi.RootGet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lingyi.RootGet.config.ScheduleConfig;
import com.lingyi.RootGet.entry.Collection;
import com.lingyi.RootGet.entry.Comment;
import com.lingyi.RootGet.entry.Video;
import com.lingyi.RootGet.mapper.video.CollectionMapper;
import com.lingyi.RootGet.mapper.video.CommentMapper;
import com.lingyi.RootGet.mapper.video.AccountMapper;
import com.lingyi.RootGet.mapper.video.VideoMapper;
import com.lingyi.RootGet.tools.RedisTools;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RootGetApplicationTests {
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTools redisTools;
    @Autowired
    private ScheduleConfig scheduleConfig;
    @Autowired
    private CollectionMapper collectionMapper;
    @Test
    void contextLoads(){
        
    }

}