package com.lingyi.RootGet.mapper.web;

import com.lingyi.RootGet.entry.Words;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WordsMapper {
    List<Words> getWords();
}
