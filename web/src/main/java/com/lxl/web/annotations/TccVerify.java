package com.lxl.web.annotations;

import com.lxl.common.enums.TransactionEnum;

import java.lang.annotation.*;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TccVerify {
    TransactionEnum transaction();
}
