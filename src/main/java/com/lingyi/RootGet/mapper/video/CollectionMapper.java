package com.lingyi.RootGet.mapper.video;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CollectionMapper {
    @MapKey("name")
    Map<String,Object> selectAll();
    boolean isExists(String name);
    void addCollections(@Param("list") Map<String,String> map);
    void updateCollections(@Param("list") Map<String,String> map);
    void delete(List<String> collectionKeys);
}
