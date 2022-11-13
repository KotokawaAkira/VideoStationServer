package com.lingyi.RootGet.mapper.video;

import com.lingyi.RootGet.entry.Account;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.annotation.NonNull;

import java.util.List;


@Mapper
public interface AccountMapper {
    Account selectOneById(@NonNull  long id);
    List<Account> selectByKeywords(String keywords);
    void addOne(@NonNull Account account);
    void updateOne(@NonNull Account account);
}
