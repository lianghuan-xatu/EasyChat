package com.xatu.easyChat.service.Impl;

import com.xatu.easyChat.entity.User;
import com.xatu.easyChat.mapper.UserMapper;
import com.xatu.easyChat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;
    @Override
    public User getUserById(String uid) {
        return userMapper.selectByPrimaryKey(uid);
    }
}
