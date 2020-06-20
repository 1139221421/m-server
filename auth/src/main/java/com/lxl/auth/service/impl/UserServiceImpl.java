package com.lxl.auth.service.impl;

import com.lxl.auth.dao.UserMapper;
import com.lxl.auth.service.UserService;
import com.lxl.common.entity.auth.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public void addUser(User user) {
        userMapper.insert(user);
    }
}
