package com.lxl.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lxl.common.enums.CodeEnum;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.utils.config.ConfUtil;
import com.lxl.web.annotations.Logined;
import com.lxl.web.annotations.Permission;
import com.lxl.web.redis.RedisCacheUtils;
import com.lxl.web.support.AuthCurrentUser;
import com.lxl.web.support.OperatorBase;
import com.lxl.web.support.OperatorUtils;
import com.lxl.web.utils.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * 登录、权限拦截器基类
 */
public abstract class BaseAuthInterceptor<T extends OperatorBase> extends HandlerInterceptorAdapter {
    private static Logger logger = LoggerFactory.getLogger(BaseAuthInterceptor.class);

    @Resource
    private OperatorUtils operatorUtils;

    @Autowired
    RedisCacheUtils redisCacheUtils;

    /**
     * 拦截登录&权限
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug("###############进入拦截器###############");
        String path = (request.getRequestURI().replace(request.getContextPath(), ""));
        //请求客户端ip
        String ipAddr = IpUtils.getIpAddr(request);
        StringBuilder permissionName = new StringBuilder();

        boolean loginVerify = false;
        HandlerMethod hMethod = null;
        String beanName = null;
        if (handler instanceof HandlerMethod) {
            hMethod = ((HandlerMethod) handler);
            Object bean = hMethod.getBean();
            Logined loginedClassAnnotation = bean.getClass().getAnnotation(Logined.class);
            Logined loginedMethodAnnotation = hMethod.getMethodAnnotation(Logined.class);
            if (loginedClassAnnotation != null) {
                loginVerify = !loginedClassAnnotation.notEffectSelf();
            }
            if (loginedMethodAnnotation != null) {
                loginVerify = !loginedMethodAnnotation.notEffectSelf();
            }

            Permission classPermission = bean.getClass().getAnnotation(Permission.class);
            Permission methodPermission = hMethod.getMethodAnnotation(Permission.class);
            if (methodPermission != null) {
                if (methodPermission.alone()) {
                    //如果是独立的权限验证（不考虑类权限）
                    permissionName.append(methodPermission.value());
                } else if (classPermission != null && StringUtils.isNotBlank(methodPermission.value())) {
                    permissionName.append(classPermission.value());
                    permissionName.append(":");
                    permissionName.append(methodPermission.value());
                }
                beanName = bean.getClass().getName();
            }

            if (loginVerify) {
                Class<T> tClass = (Class<T>) ReflectionKit.getSuperClassGenericType(getClass(), 0);
                T loginUser = operatorUtils.getTokenUser(request, tClass);
                if (loginUser != null) {
                    AuthCurrentUser.setUser(loginUser);
                }
                if (loginUser == null) {
                    //未登录
                    notLogin(response);
                    logger.error(beanName + " path:" + path + " [未登录]");
                    return false;
                }

                //自动重置有效时间
                operatorUtils.extendUserToken(loginUser);

                if (Boolean.parseBoolean(ConfUtil.getPropertyOrDefault("security_ip_check", "false")) && !loginUser.getCurrentLoginIp().equals(ipAddr)) {
                    logger.warn("当前用户[{}]登录IP[{}]地址发生变化！，IP[{}]拒绝访问！", loginUser.getUsername(), loginUser.getCurrentLoginIp(), ipAddr);
                    return false;
                }
                request.setAttribute("loginUser", loginUser);
                if (StringUtils.isNotBlank(permissionName)) {
                    String permissionNameStr = permissionName.toString();
                    logger.info("当前用户【{}】 请求权限【{}】", loginUser.getUsername(), permissionNameStr);
                    //角色权限判断
                    if (!verifyPerms(loginUser.getHasRoleId(), permissionNameStr)) {
                        //没有权限
                        notPermission(response);
                        logger.error(beanName + " loginId:" + loginUser.getId() + " loginName:" + loginUser.getUsername() + " funcId:" + permissionName + " [没有权限]");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean verifyPerms(Long roleId, String permissionName) {
        if (roleId == null) {
            return false;
        } else {
            Set<String> permissions = getRolePerms(roleId);
            return permissions.contains(permissionName);
        }
    }

    private boolean verifyPerms(Set<Long> roleIds, String permissionName) {
        if (roleIds != null) {
            for (Long id : roleIds) {
                boolean result = verifyPerms(id, permissionName);
                if (result) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView mav) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception excptn) throws Exception {
        AuthCurrentUser.removeUser();
    }

    /**
     * 返回未登录
     *
     * @param response
     * @throws Exception
     */
    protected void notLogin(HttpServletResponse response) throws Exception {
        initResponse(response);
        response.getWriter().print(JSON.toJSON(ResponseInfo.createCodeEnum(CodeEnum.NOT_LOGIN)));
    }

    /**
     * 返回无权限
     *
     * @param response
     * @throws Exception
     */
    protected void notPermission(HttpServletResponse response) throws Exception {
        initResponse(response);
        response.getWriter().print(JSON.toJSON(ResponseInfo.createCodeEnum(CodeEnum.NO_PERMISSION)));
    }

    /**
     * 设置返回类型
     *
     * @param response
     * @throws Exception
     */
    protected void initResponse(HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
    }


    protected abstract Set<String> getRolePerms(Long roleId);

}
