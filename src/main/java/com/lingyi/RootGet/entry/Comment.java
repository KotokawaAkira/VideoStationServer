package com.lingyi.RootGet.entry;

import com.lingyi.RootGet.tools.Constant;

import java.util.List;

public class Comment {
    private String text,time,userName;
    private long id,uid,time_long,like,forId;
    private List<Comment> commentList;

    public String getText() {
        return text;
    }

    public Comment setText(String text) {
        this.text = text;
        return this;
    }

    public String getTime() {
        return time;
    }

    public Comment setTime(String time) {
        this.time = time;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public Comment setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public long getTime_long() {
        if(this.time_long==0)
            this.time_long = Constant.StringParseToLong(time);
        return time_long;
    }

    public Comment setTime_long(long time_long) {
        this.time_long = time_long;
        return this;
    }

    public long getLike() {
        return like;
    }

    public Comment setLike(long like) {
        this.like = like;
        return this;
    }

    public List<Comment> getCommentList() {
        return commentList;
    }

    public Comment setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
        return this;
    }

    public long getId() {
        return id;
    }

    public Comment setId(long id) {
        this.id = id;
        return this;
    }

    public long getForId() {
        return forId;
    }

    public Comment setForId(long forId) {
        this.forId = forId;
        return this;
    }

    public long getUid() {
        return uid;
    }

    public Comment setUid(long uid) {
        this.uid = uid;
        return this;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "text='" + text + '\'' +
                ", time='" + time + '\'' +
                ", userName='" + userName + '\'' +
                ", id=" + id +
                ", uid=" + uid +
                ", time_long=" + time_long +
                ", like=" + like +
                ", forId=" + forId +
                ", commentList=" + commentList +
                '}';
    }
}
