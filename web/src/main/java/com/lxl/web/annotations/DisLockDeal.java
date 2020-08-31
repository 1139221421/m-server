package com.lxl.web.annotations;

import com.lxl.common.enums.TagsEnum;

import java.lang.annotation.*;

/**
 * 分布式锁注解方式
 *
 * @author
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisLockDeal {

    /**
     * 业务标签
     */
    TagsEnum tag();

    /**
     * 例如：xxx（xxx为锁名称）或者例如：#p0.id(第一个参数的id字段为锁名称)
     */
    String lock();
}
