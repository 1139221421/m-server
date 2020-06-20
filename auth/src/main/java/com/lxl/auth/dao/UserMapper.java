package com.lxl.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxl.common.entity.auth.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user")
    List<User> findAll();
}
