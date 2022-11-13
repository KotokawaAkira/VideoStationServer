package com.lingyi.RootGet.mapper.video;

import com.lingyi.RootGet.entry.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Mapper
public interface CommentMapper {
    Comment getCommentsById(long id);
    List<Comment> getCommentsByForId(long forId);
    int addComment(Comment comment);
    int deleteComment(Comment comment);
    void updateName(String oldName,String newName);
}
