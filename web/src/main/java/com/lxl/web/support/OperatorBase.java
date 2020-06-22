package com.lxl.web.support;

import lombok.Data;

import java.util.Set;

/**
 * @description: 当前登录用户基础类
 */
@Data
public class OperatorBase {
    /**
     * 当前登录用户名
     */
    protected String username;
    /**
     * 当前登录用户id
     */
    protected Long id;

    protected String token;
    /**
     * 当前登录账号手机号
     */
    protected String tel;
    /**
     * 拥有的角色id
     */
    protected Set<Long> hasRoleId;

    /**
     * 当前登录ip
     */
    protected String currentLoginIp;

    /**
     * 登录用户类型
     */
    protected String userLoginType;

}
