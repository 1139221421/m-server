package com.lxl.auth.interceptor;

import com.lxl.web.interceptor.BaseAuthInterceptor;
import com.lxl.web.support.OperatorBase;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * 获取权限集合
 */
@Configuration
public class AuthInterceptor extends BaseAuthInterceptor<OperatorBase> {

    @Override
    protected Set<String> getRolePerms(Long roleId) {
        //todo 获取权限集合
        return new HashSet<>();
    }

}
