package com.lxl.web.support;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lxl.common.constance.Constance;
import com.lxl.utils.common.Md5Utils;
import com.lxl.utils.config.ConfUtil;
import com.lxl.web.redis.RedisCacheUtils;
import com.lxl.web.utils.IpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Component
public class OperatorUtils {

    @Resource
    private RedisCacheUtils redisCacheUtils;

    @Value("${auth.expirationSecond:7200}")
    private int expirationSecond;

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    /**
     * 获取后台管理登录信息
     *
     * @return
     */
    public <T extends OperatorBase> T getTokenUser(Class<T> clazz) {
        return getTokenUser(getRequest(), clazz);
    }

    /**
     * 获取后台管理登录信息
     *
     * @return
     */
    public OperatorBase getTokenUser() {
        return getTokenUser(getRequest(), OperatorBase.class);
    }

    /**
     * 获取后台管理登录信息
     *
     * @return
     */
    public <T extends OperatorBase> T getUser(Class<T> clazz) {
        Object obj = getRequest().getAttribute(Constance.User.ATTRIBUTE_KEY);
        if (obj == null) {
            obj = getTokenUser(clazz);
        }
        return obj == null ? null : (T) obj;
    }

    /**
     * 获取后台管理登录信息createToken
     *
     * @return
     */
    public OperatorBase getTokenUser(HttpServletRequest request) {
        return getTokenUser(request, OperatorBase.class);
    }

    /**
     * 获取后台管理登录信息
     *
     * @return
     */
    public <T extends OperatorBase> T getTokenUser(HttpServletRequest request, Class<T> clazz) {
        String token = request.getHeader(Constance.User.AUTHORIZATION);
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(Constance.User.AUTHORIZATION);
        }
        if (StringUtils.isNotBlank(token)) {
            return redisCacheUtils.getCacheObject(Constance.User.USER_SESSION + token, clazz);
        } else {
            return null;
        }
    }

    /**
     * 设置后台管理登录信息
     *
     * @return
     */
    public <T extends OperatorBase> String createToken(T loginUser) {
        return createToken(loginUser, expirationSecond);
    }


    /**
     * 设置后台管理登录信息
     *
     * @return
     */
    public <T extends OperatorBase> String createToken(T loginUser, long seconds) {
        String token = Md5Utils.hash(Md5Utils.getUUID() + loginUser.getId());
        loginUser.setToken(token);
        loginUser.setCurrentLoginIp(IpUtils.getIpAddr(getRequest()));
        String tokenKey = Constance.User.TOKEN_USER + loginUser.getUserLoginType() + "_" + loginUser.getId();
        String sessionKey = Constance.User.USER_SESSION + token;
        // 查询该用户有没有登陆
        String t = (String) redisCacheUtils.getCacheObject(tokenKey);
        if (t != null && redisCacheUtils.exist(sessionKey)) {
            // 之前登陆还存在 直接踢了
            redisCacheUtils.delete(sessionKey);
            redisCacheUtils.delete(tokenKey);
        }
        // 将用户登录对应的token存起来 方便用户更新的时候更新redis
        redisCacheUtils.setCacheObject(sessionKey, loginUser, seconds);
        redisCacheUtils.setCacheObject(tokenKey, token, seconds);
        return token;
    }

    /**
     * 移除登录用户
     *
     * @param token
     */
    public void removeLoginUser(String token) {
        redisCacheUtils.delete(Constance.User.USER_SESSION + token);
        OperatorBase operator = getUser(OperatorBase.class);
        if (operator != null) {
            redisCacheUtils.delete(Constance.User.TOKEN_USER + operator.getUserLoginType() + "_" + operator.getId());
        }
    }

    /**
     * 获取后台管理登录用户id
     *
     * @return
     */
    public Long getLoginUserId() {
        OperatorBase operator = getUser(OperatorBase.class);
        if (operator != null) {
            return operator.getId();
        }
        return null;
    }

    /**
     * 获取用户token的剩余有效期，判断并自动延期
     *
     * @return
     */
    public Long getUserExpiration() {
        String token = getRequest().getHeader(Constance.User.AUTHORIZATION);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        String redisKey = Constance.User.USER_SESSION + token;
        Long expire = redisCacheUtils.getRedisTemplate().getExpire(redisKey, TimeUnit.MINUTES);
        return expire;
    }

    /**
     * 重置登录时间
     */
    public void extendUserToken(OperatorBase loginUser) {
        if (getUserExpiration() != null && getUserExpiration() < Long.valueOf(ConfUtil.getPropertyOrDefault("auto_extend_time_valve", "30"))) {
            redisCacheUtils.expire(Constance.User.USER_SESSION + loginUser.getToken(), 120, TimeUnit.MINUTES);
            redisCacheUtils.expire(Constance.User.TOKEN_USER + loginUser.getUserLoginType() + "_" + loginUser.getId(), 120, TimeUnit.MINUTES);
        }
    }

}
