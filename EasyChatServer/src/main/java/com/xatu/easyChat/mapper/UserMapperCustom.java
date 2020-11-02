package com.xatu.easyChat.mapper;


import com.xatu.easyChat.entity.vo.FriendRequestVo;
import com.xatu.easyChat.entity.vo.MyFriendsVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapperCustom {
    /**
     * 查询所有好友信息
     * @param userId
     * @return
     */
    public List<MyFriendsVO> queryMyFriends(String userId);

    List<FriendRequestVo> queryFriendRequestList(String userId);
}
