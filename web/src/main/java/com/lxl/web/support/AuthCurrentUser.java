package com.lxl.web.support;

/**
 * 当前登陆用户
 */
public class AuthCurrentUser {

    private final static ThreadLocal<OperatorBase> userLocal = new ThreadLocal<>();

    public static OperatorBase getUser() {
        return userLocal.get();
    }

    public static void setUser(OperatorBase user) {
        userLocal.set(user);
    }

    public static Long getUserId() {
        if (getUser() != null) {
            return getUser().getId();
        }
        return null;
    }

    public static void removeUser() {
        userLocal.remove();
    }
}
