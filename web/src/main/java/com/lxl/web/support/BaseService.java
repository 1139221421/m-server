package com.lxl.web.support;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxl.common.entity.BaseEntity;
import com.lxl.web.utils.HttpServletUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 基础增删改查service
 *
 * @author：
 */
public class BaseService<M extends BaseMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> {

    protected HttpServletRequest getRequest() {
        return HttpServletUtils.getRequest();
    }

    protected HttpServletResponse getResponse() {
        return HttpServletUtils.getResponse();
    }


    /**
     * 获取所有请求参数
     *
     * @return 返回所有请求参数
     */
    protected Map<String, String> getAllParam() {
        return HttpServletUtils.getAllParam();
    }

    /**
     * 获取请求参数
     *
     * @return 返回所有请求参数
     */
    protected Object getRequestParam(String key) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getParameter(key);
        }
        return null;
    }

    /**
     * 获得请求参数返回指定的强制转换对象
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    protected <T> T getRequestParam(String key, Class<T> clazz) {
        Object obj = getRequestParam(key);
        if (obj != null) {
            return (T) obj;
        }
        return null;
    }

    /**
     * 获得请求参数返回字符串
     *
     * @param key
     * @return
     */
    protected String getRequestParamString(String key) {
        Object obj = getRequestParam(key);
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    @Override
    protected Class<T> currentModelClass() {
        return (Class<T>) ReflectionKit.getSuperClassGenericType(getClass(), 1);
    }
}
