package com.lxl.web.annotations;

import java.lang.annotation.*;


/**
 * @description: 权限注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {

    /**
     * 权限内容
     */
    String value() default "";

    /**
     * 是否是独立的
     * true 不和类注解的权限名组合
     * false 没有类注解权限则无效
     */
    boolean alone() default false;


}
