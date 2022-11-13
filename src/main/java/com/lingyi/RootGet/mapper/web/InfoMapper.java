package com.lingyi.RootGet.mapper.web;

import com.lingyi.RootGet.entry.Info;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InfoMapper {
    List<Info> getInfo();
}
