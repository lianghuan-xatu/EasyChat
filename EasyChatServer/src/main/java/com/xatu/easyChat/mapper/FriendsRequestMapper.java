package com.xatu.easyChat.mapper;

import com.xatu.easyChat.entity.FriendsRequest;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendsRequestMapper {
    int deleteByPrimaryKey(String id);

    int insert(FriendsRequest record);

    int insertSelective(FriendsRequest record);

    FriendsRequest selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FriendsRequest record);

    int updateByPrimaryKey(FriendsRequest record);

    void deleteByFriendRequest(FriendsRequest friendsRequest);
}