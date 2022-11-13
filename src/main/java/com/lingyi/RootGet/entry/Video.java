package com.lingyi.RootGet.entry;

import com.lingyi.RootGet.tools.Constant;
import org.springframework.util.StringUtils;

public class Video {
    private String id,title,summary,time,upName;
    private long up,time_long,like,collection,play;

    public long getPlay() {
        return play;
    }

    public Video setPlay(long play) {
        this.play = play;
        return this;
    }

    public String getUpName() {
        return upName;
    }

    public Video setUpName(String upName) {
        this.upName = upName;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Video setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public Video setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public String getTime() {
        return time;
    }

    public Video setTime(String time) {
        this.time = time;
        return this;
    }

    public String getId() {
        return id;
    }

    public Video setId(String id) {
        this.id = id;
        return this;
    }

    public long getUp() {
        return up;
    }

    public Video setUp(long up) {
        this.up = up;
        return this;
    }

    public long getTime_long() {
        if(time_long==0L&& StringUtils.hasText(time))
            time_long = Constant.StringParseToLong(time);
        return time_long;
    }

    public Video setTime_long(long time_long) {
        this.time_long = time_long;
        return this;
    }

    public long getLike() {
        return like;
    }

    public Video setLike(long like) {
        this.like = like;
        return this;
    }

    public long getCollection() {
        return collection;
    }

    public Video setCollection(long collection) {
        this.collection = collection;
        return this;
    }

    @Override
    public String toString() {
        return "Video{" +
                "title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", time='" + time + '\'' +
                ", upName='" + upName + '\'' +
                ", id=" + id +
                ", up=" + up +
                ", time_long=" + time_long +
                ", like=" + like +
                ", collection=" + collection +
                ", play=" + play +
                '}';
    }
}
