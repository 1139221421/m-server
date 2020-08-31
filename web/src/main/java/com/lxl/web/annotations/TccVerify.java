package com.lxl.web.annotations;

import com.lxl.common.enums.TagsEnum;
import com.lxl.common.enums.TransactionEnum;

import java.lang.annotation.*;

/**
 * TCC事务验证注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TccVerify {
    /**
     * TCC事务执行阶段
     */
    TransactionEnum transaction();

    /**
     * 锁名称 与lockId应该同时存在
     */
    TagsEnum lockName() default TagsEnum.TEST;

    /**
     * TCC-prepare阶段 资源锁定-通过lock获取分布式锁lockId(#p0.obj.id:第一个参数的obj字段下的id字段值作为lockId)
     */
    String lockId() default "";
}
