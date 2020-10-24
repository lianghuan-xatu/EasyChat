package com.xatu.easyChat.mapper;

import com.xatu.easyChat.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {
    int deleteByPrimaryKey(String id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByUserName(String username);
}