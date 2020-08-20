package com.lxl.web.interceptor;

import com.lxl.web.support.OperatorBase;
import java.util.HashSet;
import java.util.Set;

/**
 * 默认获取权限集合为空集合
 */
public class DefaultAuthInterceptor extends BaseAuthInterceptor<OperatorBase> {

    @Override
    protected Set<String> getRolePerms(Long roleId) {
        return new HashSet<>();
    }

}
