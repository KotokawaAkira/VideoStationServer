package com.lingyi.RootGet.entry;

import java.util.Set;

public class Collection {
    private String name;
    private Set<Video> videos;

    public String getName() {
        return name;
    }

    public Collection setName(String name) {
        this.name = name;
        return this;
    }

    public Set<Video> getVideos() {
        return videos;
    }

    public void setVideos(Set<Video> videos) {
        this.videos = videos;
    }

    @Override
    public String toString() {
        return "Collection{" +
                "name='" + name + '\'' +
                ", videos=" + videos +
                '}';
    }
}
