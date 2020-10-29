package com.xatu.easyChat.service.Impl;

import com.xatu.easyChat.entity.User;
import com.xatu.easyChat.mapper.UserMapper;
import com.xatu.easyChat.service.UserService;
import com.xatu.easyChat.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    IdWorker idWorker;


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
        return userMapper.insert(user);
    }

    @Override
    public User updateUserInfo(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        return userMapper.selectByPrimaryKey(user.getId());
    }

    @Override
    public void updateUserAvatar(String userId, String fileName) {
        userMapper.updateUserAvatar(userId,fileName);
    }


}
