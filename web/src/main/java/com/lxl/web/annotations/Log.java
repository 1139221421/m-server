package com.lxl.web.annotations;

import com.lxl.common.enums.LogTypeEnum;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * @author：
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 日志类型
     *
     * @return
     */
    LogTypeEnum value() default LogTypeEnum.SYSTEM_LOG;

}
