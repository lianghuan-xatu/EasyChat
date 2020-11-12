package com.xatu.easyChat.mapper;

import com.xatu.easyChat.entity.ChatMsg;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMsgMapper {
    int deleteByPrimaryKey(String id);

    int insert(ChatMsg record);

    int insertSelective(ChatMsg record);

    ChatMsg selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChatMsg record);

    int updateByPrimaryKey(ChatMsg record);

    List<ChatMsg> getUnReadMsgListByAcceptUid(String acceptUserId);
}