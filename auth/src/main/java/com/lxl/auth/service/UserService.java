package com.lxl.auth.service;

import com.lxl.common.entity.auth.User;

import java.util.List;

public interface UserService {

    List<User> findAll();

    void addUser(User user);
}
