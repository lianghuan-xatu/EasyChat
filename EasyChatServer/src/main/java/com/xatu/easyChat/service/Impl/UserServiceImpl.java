package com.xatu.easyChat.service.Impl;

import com.xatu.easyChat.easyChatServer.ChatMsg;
import com.xatu.easyChat.easyChatServer.DataContent;
import com.xatu.easyChat.entity.FriendsRequest;
import com.xatu.easyChat.entity.MyFriends;
import com.xatu.easyChat.entity.User;
import com.xatu.easyChat.entity.UserChannelRel;
import com.xatu.easyChat.entity.enums.MsgActionEnum;
import com.xatu.easyChat.entity.enums.MsgSignFlagEnum;
import com.xatu.easyChat.entity.enums.SearchFriendsStatusEnum;
import com.xatu.easyChat.entity.vo.FriendRequestVo;
import com.xatu.easyChat.entity.vo.MyFriendsVO;
import com.xatu.easyChat.mapper.*;
import com.xatu.easyChat.service.UserService;
import com.xatu.easyChat.utils.*;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jdk.nashorn.internal.runtime.JSONFunctions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service

public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    IdWorker idWorker;
    @Autowired
    MyFriendsMapper myFriendsMapper;
    @Autowired
    FriendsRequestMapper friendsRequestMapper;
    @Autowired
    UserMapperCustom userMapperCustom;
    @Autowired
    ChatMsgMapper chatMsgMapper;


    @Override
    public User getUserById(String uid) {
        return userMapper.selectByPrimaryKey(uid);
    }

    @Override
    public User queryUserIsExist(String username) {
        return userMapper.selectByUserName(username);
    }

    @Override
    public int insert(User user) {
        long id = idWorker.nextId();
        user.setId(String.valueOf(id));

        //生成用户二维码
        String prefix = "qrCodeImage";
        String suffix = ".png";
        File filePath = new File("D://test2/");
        if(!filePath.exists()) {
            filePath.mkdir();
        }
        String qrCodePath = filePath.getPath() + prefix + suffix;
        try {
            File.createTempFile(prefix, suffix, filePath);
            QRCodeUtils.createQRCode(qrCodePath, "qrcode:"+user.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultipartFile multipartFile = FileUtils.fileToMultipart(qrCodePath);
        //将二维码上传至OSS
        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String formatDate = simpleDateFormat.format(currentDate);
        String fileName = id + "/" + formatDate + "/" + "qrCodeImage.png";
        String resultUrl = OSSUtils.uploadMultiPartFile(fileName, multipartFile);
        //设置数据库二维码url
        user.setQrcode(fileName);
        return userMapper.insert(user);
    }

    @Override
    public User updateUserInfo(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        return userMapper.selectByPrimaryKey(user.getId());
    }

    @Override
    public void updateUserAvatar(String userId, String fileName) {
        userMapper.updateUserAvatar(userId, fileName);
    }

    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUserName) {
        User user = userMapper.selectByUserName(friendUserName);
        if(user == null) {
            //添加的用户不存在
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        if(myUserId.equals(user.getId())) {
            //不能添加自己
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        //在好友列表搜索查询是否已添加
        MyFriends myFriends = new MyFriends();
        myFriends.setId(myUserId);
        myFriends.setMyFriendUserId(user.getId());
        MyFriends friend = myFriendsMapper.selectOneByExample(myFriends);
        if(friend != null) {
            //已添加好友
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Override
    public void sendFriendRequest(String myUserId, String friendUserName) {
        User friend = queryUserIsExist(friendUserName);
        String friendId = friend.getId();
        //通过friend查询好友列表看是否已添加好友
        MyFriends myFriends = new MyFriends();
        myFriends.setMyUserId(myUserId);
        myFriends.setMyFriendUserId(friendId);
        MyFriends myExistFriends = myFriendsMapper.selectOneByExample(myFriends);
        if(myExistFriends == null) {
            //向FriendsRequest表中添加信息
            FriendsRequest friendsRequest = new FriendsRequest();
            friendsRequest.setId(String.valueOf(new IdWorker().nextId()));
            friendsRequest.setSendUserId(myUserId);
            friendsRequest.setAcceptUserId(friendId);
            friendsRequest.setRequestDateTime(new Date());
            friendsRequestMapper.insert(friendsRequest);
        }

    }

    /**
     * 删除好友请求
     * @param friendsRequest
     */
    @Override
    public void deleteFriendRequest(FriendsRequest friendsRequest) {
        friendsRequestMapper.deleteByFriendRequest(friendsRequest);
    }

    /**
     * 删除好友请求并向好友列表添加新的好友信息
     * @param sendUserId
     * @param acceptUserId
     */
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        //进行双向好友数据保存
        saveFriends(sendUserId,acceptUserId);
        saveFriends(acceptUserId,sendUserId);

        //删除好友请求表中的数据
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setSendUserId(sendUserId);
        friendsRequest.setAcceptUserId(acceptUserId);
        deleteFriendRequest(friendsRequest);


        //主动发送消息更新发送方好友通讯录列表
        Channel sendChannel  = UserChannelRel.get(sendUserId);
        if(sendChannel!=null){
            //使用websocket 主动推送消息到请求发起者，更新他的通讯录列表为最新
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

            //消息的推送
            sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }

    }

    /**
     * 查询列表所有好友
     * @param
     * @return
     */
    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {
        return userMapperCustom.queryMyFriends(userId);
    }

    @Override
    public List<FriendRequestVo> queryFriendRequestList(String userId) {
        return userMapperCustom.queryFriendRequestList(userId);
    }

    @Override
    public String saveMsg(ChatMsg chatMsg) {
        com.xatu.easyChat.entity.ChatMsg chatMsgInfo = new com.xatu.easyChat.entity.ChatMsg();
        chatMsgInfo.setId(String.valueOf(new IdWorker().nextId()));
        chatMsgInfo.setSendUserId(chatMsg.getSenderId());
        chatMsgInfo.setAcceptUserId(chatMsg.getReceiverId());
        chatMsgInfo.setMsg(chatMsg.getMsg());
        chatMsgInfo.setSignFlag(MsgSignFlagEnum.unsign.getStatus());
        chatMsgInfo.setCreateTime(new Date());
        chatMsgMapper.insert(chatMsgInfo);
        return chatMsgInfo.getId();

    }

    /**
     * 批量签收用户消息
     * @param msgIdList
     */
    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        userMapperCustom.batchUpdateMsgSigned(msgIdList);

    }

    //通过好友请求并保存数据到my_friends 表中
    private void saveFriends(String sendUserId, String acceptUserId){
        MyFriends myFriends = new MyFriends();
        String recordId = String.valueOf(new IdWorker().nextId());
        myFriends.setId(recordId);
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);

        myFriendsMapper.insert(myFriends);
    }

}
