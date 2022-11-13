package com.lingyi.RootGet.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.RootGet.entry.Video;
import com.lingyi.RootGet.mapper.video.CollectionMapper;
import com.lingyi.RootGet.mapper.video.VideoMapper;
import com.lingyi.RootGet.tools.RedisTools;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

@Configuration
@EnableScheduling
public class ScheduleConfig {
    private final RedisTools redisTools;
    private final VideoMapper videoMapper;
    private final CollectionMapper collectionMapper;
    private final ObjectMapper objectMapper;

    public ScheduleConfig(RedisTools redisTools, VideoMapper videoMapper, CollectionMapper collectionMapper, ObjectMapper objectMapper) {
        this.redisTools = redisTools;
        this.videoMapper = videoMapper;
        this.collectionMapper = collectionMapper;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void sync() throws JsonProcessingException {
        syncVideo();
        syncCollection();
    }

    private void syncVideo() {
        List<Video> list = new ArrayList<>();
        List<Video> exists = new ArrayList<>();
        List<Video> notExists = new ArrayList<>();
        //获取所有id
        Set<String> keys = redisTools.getKeys("VideoStation:Video:*");

        if (keys == null || keys.isEmpty()) return;
        //redis中获取视频
        for (String key : keys) {
            Video video = redisTools.getFromHashAsObject(key, Video.class);
            list.add(video);
        }
        //所有添加到list
        for (Video video : list) {
            if (videoMapper.isExists(video.getId()))
                exists.add(video);
            else notExists.add(video);
        }

        if (!exists.isEmpty())
            videoMapper.updateVideos(exists);
        if (!notExists.isEmpty())
            videoMapper.addVideos(notExists);

        //清除
        list.clear();
        exists.clear();
        notExists.clear();
    }

    public void syncCollection() throws JsonProcessingException {
        Map<String, String> exists_collection = new HashMap<>();
        Map<String, String> notExists_collection = new HashMap<>();

        List<String> noLongerUse = new ArrayList<>();

        //获取所有id
        Set<String> keys = redisTools.getKeys("VideoStation:Collection:*");
        if (keys == null || keys.isEmpty()) return;

        //所有添加到list
        for (String key : keys) {
            Set<String> set = redisTools.getRedisTemplate().opsForSet().members(key);
            String json = objectMapper.writeValueAsString(set);
            if (collectionMapper.isExists(key))
                exists_collection.put(key, json);
            else
                notExists_collection.put(key, json);
        }

        if (!exists_collection.isEmpty())
            collectionMapper.updateCollections(exists_collection);
        if (!notExists_collection.isEmpty())
            collectionMapper.addCollections(notExists_collection);

        Map<String, Object> selectAll = collectionMapper.selectAll();
        for (String key : selectAll.keySet()) {
            if (!keys.contains(key)){
                noLongerUse.add(key);
            }
        }

        collectionMapper.delete(noLongerUse);

        //清除
        exists_collection.clear();
        notExists_collection.clear();
    }
}
