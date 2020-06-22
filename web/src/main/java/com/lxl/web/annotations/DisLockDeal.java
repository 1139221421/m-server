package com.lxl.web.annotations;

import com.lxl.common.enums.MqTagsEnum;

import java.lang.annotation.*;

/**
 * @author raozhidan
 * @descirption
 * @date 2019-07-13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisLockDeal {

    /**
     * 处理业务
     *
     * @return
     */
    MqTagsEnum action();

    /**
     * 需要取得具体某项数据名称
     *
     * @return
     */
    String lockIdName();
}
