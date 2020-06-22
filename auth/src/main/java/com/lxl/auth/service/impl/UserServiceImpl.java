package com.lxl.auth.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lxl.auth.dao.UserMapper;
import com.lxl.auth.service.UserService;
import com.lxl.auth.vo.LoginRequestInfo;
import com.lxl.auth.vo.LoginUserInfo;
import com.lxl.common.entity.auth.User;
import com.lxl.common.entity.message.Message;
import com.lxl.common.feign.message.MessageFeign;
import com.lxl.utils.common.Md5Utils;
import com.lxl.utils.common.PasswordUtil;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private MessageFeign messageFeign;

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public void crate(User user) {
        userMapper.insert(user);
    }

    @Override
    @GlobalTransactional(name = "test", rollbackFor = Exception.class)
    public void update(User user) {
        userMapper.updateById(user);
        Message message = new Message();
        message.setTitle("test");
        messageFeign.create(message);
    }

    @Override
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public LoginUserInfo veryfiyUser(LoginRequestInfo loginRequestInfo) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", loginRequestInfo.getUsername());
        queryWrapper.last("limit 1");
        User user = userMapper.selectOne(queryWrapper);
        if (user != null && PasswordUtil.getPwd(user.getSalt(), loginRequestInfo.getPassword()).equals(user.getPassword())) {
            return new LoginUserInfo<>(user);
        }
        return new LoginUserInfo<>(user, false);
    }
}
