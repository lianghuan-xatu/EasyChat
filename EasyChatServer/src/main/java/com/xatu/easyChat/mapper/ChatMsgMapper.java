package com.xatu.easyChat.mapper;

import com.xatu.easyChat.entity.ChatMsg;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMsgMapper {
    int deleteByPrimaryKey(String id);

    int insert(ChatMsg record);

    int insertSelective(ChatMsg record);

    ChatMsg selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChatMsg record);

    int updateByPrimaryKey(ChatMsg record);
}