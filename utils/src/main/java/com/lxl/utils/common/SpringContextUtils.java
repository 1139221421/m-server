package com.lxl.utils.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName SpringContextUtils
 * @Author Zhidan.Rao
 * @Date 2019年02月21日 16:05
 * @Version 1.0.0
 **/
@Component
public class SpringContextUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<? extends T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static <T> List<T> getBeans(Class<T> clazz) {
        List<T> beans = new ArrayList<>();
        Map<String, T> beansOfType = applicationContext.getBeansOfType(clazz);
        beans.addAll(beansOfType.values());
        return beans;
    }
}
