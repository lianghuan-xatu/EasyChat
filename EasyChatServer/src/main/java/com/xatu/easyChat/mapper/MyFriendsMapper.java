package com.xatu.easyChat.mapper;

import com.xatu.easyChat.entity.MyFriends;
import org.springframework.stereotype.Repository;

@Repository
public interface MyFriendsMapper {
    int deleteByPrimaryKey(String id);

    int insert(MyFriends record);

    int insertSelective(MyFriends record);

    MyFriends selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(MyFriends record);

    int updateByPrimaryKey(MyFriends record);

    MyFriends selectOneByExample(MyFriends myFriends);
}