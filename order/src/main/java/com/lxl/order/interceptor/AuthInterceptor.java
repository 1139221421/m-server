package com.lxl.order.interceptor;

import com.lxl.web.interceptor.BaseAuthInterceptor;
import com.lxl.web.support.OperatorBase;

import java.util.HashSet;
import java.util.Set;

/**
 * 获取权限集合
 */
public class AuthInterceptor extends BaseAuthInterceptor<OperatorBase> {

    @Override
    protected Set<String> getRolePerms(Long roleId) {
        //todo 获取权限集合
        return new HashSet<>();
    }

}
