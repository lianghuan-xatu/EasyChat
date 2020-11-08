package com.xatu.easyChat.service;

import com.xatu.easyChat.easyChatServer.ChatMsg;
import com.xatu.easyChat.entity.FriendsRequest;
import com.xatu.easyChat.entity.User;
import com.xatu.easyChat.entity.vo.FriendRequestVo;
import com.xatu.easyChat.entity.vo.MyFriendsVO;

import java.util.List;

public interface UserService {
    public User getUserById(String uid);

    User queryUserIsExist(String username);

    int insert(User user);

    User updateUserInfo(User user);

    void updateUserAvatar(String userId, String fileName);

    Integer preconditionSearchFriends(String myUserId, String friendUserName);

    void sendFriendRequest(String myUserId, String friendUserName);

    void deleteFriendRequest(FriendsRequest friendsRequest);

    void passFriendRequest(String sendUserId, String acceptUserId);

    List<MyFriendsVO> queryMyFriends(String acceptUserId);

    List<FriendRequestVo> queryFriendRequestList(String userId);

    String saveMsg(ChatMsg chatMsg);

    void updateMsgSigned(List<String> msgIdList);
}
