package com.lxl.auth.service;

import com.lxl.common.entity.auth.User;

import java.util.List;

public interface UserService {

    List<User> findAll();

    void crate(User user);

    void update(User user);

    void delete(Long id);
}
