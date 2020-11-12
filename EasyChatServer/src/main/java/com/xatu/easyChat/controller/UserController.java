package com.xatu.easyChat.controller;



import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.xatu.easyChat.entity.ChatMsg;
import com.xatu.easyChat.entity.FriendsRequest;
import com.xatu.easyChat.entity.User;
import com.xatu.easyChat.entity.bo.UserBo;
import com.xatu.easyChat.entity.enums.OperatorFriendRequestTypeEnum;
import com.xatu.easyChat.entity.enums.SearchFriendsStatusEnum;
import com.xatu.easyChat.entity.vo.FriendRequestVo;
import com.xatu.easyChat.entity.vo.MyFriendsVO;
import com.xatu.easyChat.entity.vo.UserVo;
import com.xatu.easyChat.service.UserService;
import com.xatu.easyChat.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.ibatis.annotations.ResultMap;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;


    /**
     * 根据用户uid来查询用户
     * @param uid
     * @param model
     * @return
     */
    @RequestMapping("/getUserById/{uid}")
    public String getUserById(@PathVariable String uid, Model model){
        User user = userService.getUserById(uid);
        model.addAttribute("user",user);
        return "index";
    }


    /**
     * 用户登陆注册一体化方法
     * @param user
     * @return
     */
    @RequestMapping("/registerOrLogin")
    @ResponseBody
    public JSONResult registerOrLogin(@RequestBody User user) {

        //判断用户是否注册过
        User existUser = userService.queryUserIsExist(user.getUsername());
        if(existUser != null) {
            //直接登录
            String password = existUser.getPassword();
            if(!MD5Utils.getPwd(user.getPassword()).equals(password)) {
                //密码错误
                return JSONResult.errorMap("密码错误！");
            }

        }else {
            //注册用户
            user.setNickname(user.getUsername());
            user.setPassword(MD5Utils.getPwd(user.getPassword()));
            user.setFaceImage("");
            user.setFaceImageBig("");
            int insert = userService.insert(user);
            if (insert < 1) {
                return JSONResult.errorMsg("注册失败！");
            }
            existUser = userService.queryUserIsExist(user.getUsername());
        }
        System.out.println("用户" + user.getUsername() + "登录成功");
        //封装返回结果
        UserVo userVo = new UserVo();
        //注册或登录成功，将用户信息返回到前台
        BeanUtils.copyProperties(existUser,userVo);
        return JSONResult.ok(userVo);
    }


    /**
     * 用户图片上传阿里云OSS
     */
    @RequestMapping("/uploadFaceBase64")
    @ResponseBody
    public JSONResult uploadFaceBase64(@RequestBody UserBo userBo) throws Exception {
        String faceData = userBo.getFaceData();
        String userId = userBo.getUserId();
        //服务器Base64转文件 文件暂存路径
        String filePath = "D://test/" + userId + "/userFaceImage.png";
        Date curDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String format = simpleDateFormat.format(curDate);
        // String fileName = format + "/" + userId + "/userFaceImage.png";
        FileUtils.base64ToFile(filePath, faceData);
        //file转 multipartFile
        MultipartFile multipartFile = FileUtils.fileToMultipart(filePath);
        String fileName = userId + "/" + format + "/" + "userFceImage.png";
        try{
            //上传到OSS
            String resultUrl = OSSUtils.uploadMultiPartFile(fileName, multipartFile);

        }catch (Exception e){
            e.printStackTrace();
        }
        //保存更改头像信息到数据库
        userService.updateUserAvatar(userId,fileName);
        User userById = userService.getUserById(userId);
        return JSONResult.ok(userById);

    }

    /**
     * 用户修改昵称
     */
    @PostMapping("/updateNickName")
    @ResponseBody
    public JSONResult updateNickName(@RequestBody User user){
        User newUser = userService.updateUserInfo(user);
        //返回更新后的用户信息
        System.out.println(newUser);
        return JSONResult.ok(newUser);
    }


    /**
     * （添加好友）按照用户账号搜索用户
     */
    @PostMapping("/searchFriend")
    @ResponseBody
    public JSONResult searchFriend(String myUserId,String friendUserName) {
        /**
         * 进行前置条件查询
         * 查询用户为自己的账号，返回不能添加自己为好友
         * 搜索用户已经是好友，返回该用户已经是你的好友
         * 查询不到搜索用户，返回查找不到该用户
         */
        Integer status = userService.preconditionSearchFriends(myUserId,friendUserName);

        if(status == SearchFriendsStatusEnum.SUCCESS.status) {
            //返回查询好友用户信息
            User user = userService.queryUserIsExist(friendUserName);
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user,userVo);
            return JSONResult.ok(userVo);

        }else {
            return JSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
        }
    }



    /**
     * 发送添加好友请求
     */
    @RequestMapping("/addFriendRequest")
    @ResponseBody
    public JSONResult addFriendRequest(String myUserId,String friendUserName) {
        if(StringUtils.isBlank(myUserId)|| StringUtils.isBlank(friendUserName)){
            return JSONResult.errorMsg("好友信息为空");
        }

        /**
         * 前置条件：
         * 1.搜索的用户如果不存在，则返回【无此用户】
         * 2.搜索的账号如果是你自己，则返回【不能添加自己】
         * 3.搜索的朋友已经是你好友，返回【该用户已经是你的好友】
         */
        Integer status = userService.preconditionSearchFriends(myUserId,friendUserName);
        if(status==SearchFriendsStatusEnum.SUCCESS.status){
            userService.sendFriendRequest(myUserId,friendUserName);
        }else{
            String msg = SearchFriendsStatusEnum.getMsgByKey(status);
            return JSONResult.errorMsg(msg);
        }
        return JSONResult.ok();


    }


    //好友请求列表查询
    @RequestMapping("/queryFriendRequest")
    @ResponseBody
    public JSONResult queryFriendRequest(String userId){
        List<FriendRequestVo> friendRequestList = userService.queryFriendRequestList(userId);
        return JSONResult.ok(friendRequestList);
    }


    /**
     * 用户处理好友请求
     */
    @RequestMapping("/operFriendRequest")
    @ResponseBody
    public JSONResult operFriendRequest(String acceptUserId,String sendUserId,Integer operType) {
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setAcceptUserId(acceptUserId);
        friendsRequest.setSendUserId(sendUserId);
        if(operType== OperatorFriendRequestTypeEnum.IGNORE.type){
            //满足此条件将需要对好友请求表中的数据进行删除操作
            userService.deleteFriendRequest(friendsRequest);
        }else if(operType==OperatorFriendRequestTypeEnum.PASS.type){
            //满足此条件表示需要向好友表中添加一条记录，同时删除好友请求表中对应的记录
            userService.passFriendRequest(sendUserId,acceptUserId);
        }
        //查询好友表中的列表数据
        List<MyFriendsVO> myFriends = userService.queryMyFriends(acceptUserId);
        return JSONResult.ok(myFriends);
    }


    @RequestMapping("/myFriends")
    @ResponseBody
    public JSONResult myFriends(String userId) {
        if(!StringUtils.isNotBlank(userId)) {
            return JSONResult.errorMsg("用户ID为空！");
        }
        List<MyFriendsVO> myFriendsVO = userService.queryMyFriends(userId);
        return JSONResult.ok(myFriendsVO);
    }


    /**
     * 服务器重连获取未读信息
     */
    @RequestMapping("/getUnReadMsgList")
    @ResponseBody
    public JSONResult getUnReadMsgList(String acceptUserId) {
        if(!StringUtils.isNotBlank(acceptUserId)) {
            //接收消息者id为空
            return JSONResult.errorMsg("acceptUserId is NULL!");
        }else {
            List<ChatMsg> unreadMsgList = userService.getUnReadMsgList(acceptUserId);
            return JSONResult.ok(unreadMsgList);

        }
    }




}
