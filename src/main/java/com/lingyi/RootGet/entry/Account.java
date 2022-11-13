package com.lingyi.RootGet.entry;

import com.lingyi.RootGet.tools.Constant;
import org.springframework.util.StringUtils;

public class Account {
    private int banned;
    private long id, createTime_long;
    private String name, password, createTime;

    public long getId() {
        return id;
    }

    public Account setId(long id) {
        this.id = id;
        return this;
    }

    public int getBanned() {
        return banned;
    }

    public void setBanned(int banned) {
        this.banned = banned;
    }

    public long getCreateTime_long() {
        if (createTime_long == 0L&& StringUtils.hasText(createTime))
            createTime_long = Constant.StringParseToLong(createTime);
        return createTime_long;
    }

    public Account setCreateTime_long(long createTime_long) {
        this.createTime_long = createTime_long;
        return this;
    }

    public String getName() {
        return name;
    }

    public Account setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Account setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getCreateTime() {
        return createTime;
    }

    public Account setCreateTime(String createTime) {
        this.createTime = createTime;
        return this;
    }

    @Override
    public String toString() {
        return "Account{" +
                "banned=" + banned +
                ", id=" + id +
                ", createTime_long=" + createTime_long +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
