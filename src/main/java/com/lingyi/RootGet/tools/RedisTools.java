package com.lingyi.RootGet.tools;

import com.lingyi.RootGet.entry.Account;
import com.lingyi.RootGet.entry.Collection;
import com.lingyi.RootGet.entry.Comment;
import com.lingyi.RootGet.entry.Video;
import com.lingyi.RootGet.mapper.video.AccountMapper;
import com.lingyi.RootGet.mapper.video.CommentMapper;
import com.lingyi.RootGet.mapper.video.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.util.annotation.NonNull;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTools {

    private final AccountMapper accountMapper;
    private final VideoMapper videoMapper;
    private final CommentMapper commentMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisTools(AccountMapper accountMapper, VideoMapper videoMapper, CommentMapper commentMapper, StringRedisTemplate stringRedisTemplate) {
        this.accountMapper = accountMapper;
        this.videoMapper = videoMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.commentMapper = commentMapper;
    }

    public StringRedisTemplate getRedisTemplate() {
        return this.stringRedisTemplate;
    }

    public String getAccountToken(long id) {
        return stringRedisTemplate.opsForValue().get("VideoStation:Token:" + id);
    }

    public String setAccountToken(long id,long during,TimeUnit timeUnit) {
        String token = Constant.tokenGenerate(10);
        stringRedisTemplate.opsForValue().set("VideoStation:Token:" + id, token,during,timeUnit);
        return token;
    }

    /**
     * @param id ??????id;
     * @description ???redis?????????????????????redis????????????????????????mysql?????????????????????null?????????????????????redis???;
     */
    public Account selectAccountInCache(@NonNull long id) {
        Account account = getFromHashAsObject("VideoStation:Account:" + id, Account.class);
        //redis???????????????
        if (account == null) {
            //???mysql
            account = accountMapper.selectOneById(id);
            //mysql???????????????
            if (account == null)
                return null;
            //?????????
            account.getCreateTime_long();
            setAccountCache(account, 7, TimeUnit.DAYS);
        }
        return account;
    }

    public void setAccountCache(@NonNull Account account, long during, TimeUnit timeUnit) {
        setAsHash("VideoStation:Account:" + account.getId(), account, during, timeUnit);
    }

    /**
     * @param id ??????id
     * @description ???redis?????????????????????redis????????????????????????mysql?????????????????????null?????????????????????redis???
     */
    public Video selectVideoInCache(@NonNull String id) {
        Video video = getFromHashAsObject("VideoStation:Video:" + id, Video.class);
        //redis???????????????
        if (video == null) {
            //???mysql
            video = videoMapper.selectOneById(id);
            //mysql???????????????
            if (video == null)
                return null;
            //?????????
            setVideoCache(video, 7, TimeUnit.DAYS);
        }
        return video;
    }

    public void setVideoCache(@NonNull Video video, long during, TimeUnit timeUnit) {
        setAsHash("VideoStation:Video:" + video.getId(), video, during, timeUnit);
    }

    public long videoAddALike(String id) {
        return stringRedisTemplate.opsForHash().increment("VideoStation:Video:" + id, "like", 1);
    }

    public long videoAddADisLike(String id) {
        return stringRedisTemplate.opsForHash().increment("VideoStation:Video:" + id, "like", -1);
    }

    public long videoAddACollection(String id) {
        return stringRedisTemplate.opsForHash().increment("VideoStation:Video:" + id, "collection", 1);
    }

    public long videoAddADisCollection(String id) {
        return stringRedisTemplate.opsForHash().increment("VideoStation:Video:" + id, "collection", -1);
    }

    public Comment selectCommentInCache(long id) {
        Comment comment = getFromHashAsObject("VideoStation:Comment:" + id, Comment.class);
        if (comment == null) {
            comment = commentMapper.getCommentsById(id);
            if (comment == null)
                return null;
            setCommentCache(comment, 7, TimeUnit.DAYS);
        }
        return comment;
    }

    public void setCommentCache(Comment comment, long during, TimeUnit timeUnit) {
        setAsHash("VideoStation:Comment:" + comment.getId(), comment, during, TimeUnit.DAYS);
    }

    /**
     * ???redis?????????hash???????????????????????????
     * ?????? ??????null
     *
     * @param key  ????????????redis key
     * @param type ????????????????????????
     **/
    public <T> T getFromHashAsObject(String key, Class<T> type) {
        if (!StringUtils.hasText(key)) throw new NullPointerException("key must not be null");
        if (type == null) throw new NullPointerException("type must not be null");
        if (!stringRedisTemplate.hasKey(key)) return null;
        //???????????????
        Field[] fields = type.getDeclaredFields();
        T instance;
        Constructor<T> constructor;
        try {
            //????????????
            constructor = type.getConstructor();
            instance = constructor.newInstance();
            //???redis?????? ???????????????
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                String field_type = field.getType().getSimpleName();
                String value_string = (String) stringRedisTemplate.opsForHash().get(key, name);
                if (value_string == null) {
                    field.set(instance, null);
                    continue;
                }
                switch (field_type) {
                    case "String":
                        field.set(instance, value_string);
                        break;
                    case "short":
                        field.set(instance, Short.parseShort(value_string));
                        break;
                    case "Integer":
                        field.set(instance, Integer.valueOf(value_string));
                        break;
                    case "int":
                        field.set(instance, Integer.parseInt(value_string));
                        break;
                    case "Long":
                        field.set(instance, Long.valueOf(value_string));
                        break;
                    case "long":
                        field.set(instance, Long.parseLong(value_string));
                        break;
                    case "Double":
                        field.set(instance, Double.valueOf(value_string));
                        break;
                    case "float":
                        field.set(instance, Float.valueOf(value_string));
                        break;
                    case "boolean":
                        field.set(instance, Boolean.parseBoolean(value_string));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassCastException("???redis???????????????????????????????????????????????????" + type.getName());
        }
        return instance;
    }

    /**
     * ????????????????????????hash????????????redis
     *
     * @param key ????????????key
     * @param obj ??????????????????
     */
    public void setAsHash(String key, Object obj, long during, TimeUnit timeUnit) {
        if (!StringUtils.hasText(key)) throw new NullPointerException("key must not be null");
        if (obj == null) throw new NullPointerException("obj must not be null");
        if (stringRedisTemplate.hasKey(key))
            throw new RuntimeException("key exists");
        Field[] fields = obj.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String field_type = field.getType().getSimpleName();
                if (field.get(obj) == null) {
                    stringRedisTemplate.opsForHash().put(key, field.getName(), "null");
                    continue;
                }
                switch (field_type) {
                    case "String":
                        stringRedisTemplate.opsForHash().put(key, field.getName(), field.get(obj));
                        break;
                    case "long":
                    case "Long":
                    case "int":
                    case "Integer":
                    case "Double":
                    case "Float":
                    case "Char":
                    case "boolean":
                        stringRedisTemplate.opsForHash().put(key, field.getName(), String.valueOf(field.get(obj)));
                        break;
                }
            }
            //??????????????????
            if (during != -1 && timeUnit != null)
                stringRedisTemplate.expire(key, during, timeUnit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     * ???????????????????????????
     */
    public boolean setLock(String id, String type) {
        if (id == null || type == null)
            throw new NullPointerException("id and type must not be null");
        return stringRedisTemplate.opsForValue().setIfAbsent("VideoStation:" + type + "Lock:" + id, "1", 1, TimeUnit.MINUTES);
    }

    /**
     * ??????????????????????????????
     */
    public long getLockTTL(String id, String type) {
        if (id == null || type == null)
            throw new NullPointerException("id and type must not be null");
        return stringRedisTemplate.opsForValue().getOperations().getExpire("VideoStation:" + type + "Lock:" + id);
    }

    /**
     * ???????????????redis?????????video
     *
     * @param size    ?????????????????? ?????????????????????size?????????????????????
     * @param isFirst ???????????????????????????????????????video???isFirst = false ???????????????????????????video
     */
    public Set<Video> getVideos(int size, HttpSession session, boolean isFirst) {
        //????????????????????????
        if (isFirst) {
            Set<String> members = stringRedisTemplate.opsForSet().members("VideoStation:VideoSet:" + session.getId());
            for (String member : members) {
                stringRedisTemplate.opsForSet().remove("VideoStation:VideoSet:" + session.getId(), member);
            }
        }

        //??????redis???key
        Set<String> keys = getKeys("VideoStation:Video:*");
        if (keys.size() < size) size = keys.size();
        //???????????????video
        Set<Video> now_videos = new HashSet<>();
        for (String key : keys) {
            Video video = getFromHashAsObject(key, Video.class);
            Boolean isMember = stringRedisTemplate.opsForSet().isMember("VideoStation:VideoSet:" + session.getId(), String.valueOf(video.getId()));
            //?????????????????????
            if (isMember)
                continue;
            else {
                stringRedisTemplate.opsForSet().add("VideoStation:VideoSet:" + session.getId(), String.valueOf(video.getId()));
                stringRedisTemplate.expire("VideoStation:VideoSet:" + session.getId(), 1, TimeUnit.HOURS);
                now_videos.add(video);
            }
            if (now_videos.size() == size) return now_videos;
        }
        return now_videos;
    }
    public List<Collection> getCollection(long uid){
        //????????????
        List<Collection> collections = new ArrayList<>();
        //?????????????????????
        ScanOptions options = ScanOptions.scanOptions()
                .match("VideoStation:Collection:"+uid+":*")
                .count(1000)
                .build();
        Cursor<String> cursor = stringRedisTemplate.scan(options);
        List<String> keys = new ArrayList<>();
        while(cursor.hasNext()){
            String key = cursor.next();
            keys.add(key);
        }
        //??????????????????????????????
        for(int i=0;i<keys.size();i++){
            Collection collection = new Collection();
            collection.setName(keys.get(i).split(":")[3]);
            Set<Video> videos = new HashSet<>();
            Set<String> members = stringRedisTemplate.opsForSet().members(keys.get(i));
            for (String member : members) {
                Video video = getFromHashAsObject("VideoStation:Video:"+member, Video.class);
                if (video==null){
                    stringRedisTemplate.opsForSet().remove(keys.get(i),member);
                }else videos.add(video);
            }
            collection.setVideos(videos);
            collections.add(collection);
        }
        return collections;
    }
    public void addToCollection(long uid,long videoId,String collectionName){
        stringRedisTemplate.opsForSet().add("VideoStation:Collection:"+uid+":"+collectionName,String.valueOf(videoId));
        videoAddACollection(String.valueOf(videoId));
    }
    public void removeFromCollection(long uid,long videoId,String collectionName){
        stringRedisTemplate.opsForSet().remove("VideoStation:Collection:"+uid+":"+collectionName,String.valueOf(videoId));
        videoAddADisCollection(String.valueOf(videoId));
    }
    public Set<String> getKeys(String match){
        //??????redis???key
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().count(1000L).match(match).build();
        Cursor<String> cursor = stringRedisTemplate.scan(options);
        while (cursor.hasNext()) {
            String key = cursor.next();
            keys.add(key);
        }
        return keys;
    }
    public void renameKey(String key,String newKey){
        stringRedisTemplate.rename(key,newKey);
    }
    public long addOnePlay(String id){
        return stringRedisTemplate.opsForHash().increment("VideoStation:Video:"+id,"play",1);
    }
}
