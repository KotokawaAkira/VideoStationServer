package com.lingyi.RootGet.mapper.video;

import com.lingyi.RootGet.entry.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.annotation.NonNull;

import java.util.List;

@Mapper
public interface VideoMapper {
    List<Video> selectAll();
    boolean isExists(String id);
    Video selectOneById(@NonNull String id);
    List<Video> selectByKeywords(String keywords);
    List<Video> selectByUp(long up);
    void addOne(@NonNull Video video);
    void updateOne(@NonNull Video video);
    void updateVideos(@NonNull @Param("list") List<Video> list);
    void addVideos(@NonNull @Param("list") List<Video> list);
    void deleteOne(String id);
    void updateUpName(String oldName,String newName);
}
