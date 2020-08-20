package com.lxl.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxl.common.entity.auth.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user")
    List<User> findAll();

    @Select("update user set account_balance = account_balance - #{reduce} where id = #{id}")
    void reduceAccountBalance(@Param("id") Long id, @Param("reduce") BigDecimal reduce);
}
