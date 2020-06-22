package com.lxl.auth.vo;

import com.lxl.common.entity.BaseEntity;
import com.lxl.common.entity.auth.User;
import com.lxl.web.support.OperatorBase;
import lombok.Data;

/**
 * 登录用户
 */
@Data
public class LoginUserInfo<T extends BaseEntity> extends OperatorBase {
    private boolean success;
    /**
     * 用户信息
     */
    private T m;

    public LoginUserInfo(T user) {
        this(user, true);
    }

    public LoginUserInfo(T user, boolean success) {
        this.m = user;
        this.success = success;
        if (user instanceof User) {
            setUserInfo((User) user);
        }
    }

    private void setUserInfo(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }

}
