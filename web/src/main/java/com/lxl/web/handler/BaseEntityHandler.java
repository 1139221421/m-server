package com.lxl.web.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.lxl.common.entity.BaseEntity;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @description: mybatis plus公共字段填充处理器
 * @author：
 */
public class BaseEntityHandler implements MetaObjectHandler {

    /**
     * 测试 user 表 name 字段为空自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Date createTime = null;
        Long creatorId = null;
        if (metaObject.getOriginalObject() instanceof BaseEntity) {
            createTime = ((BaseEntity) metaObject.getOriginalObject()).getCreateTime();
            creatorId = ((BaseEntity) metaObject.getOriginalObject()).getCreatorId();
        } else if (metaObject.getOriginalObject() instanceof MapperMethod.ParamMap) {
            createTime = (Date) metaObject.getValue(Constants.ENTITY_DOT + "createTime");
            creatorId = (Long) metaObject.getValue(Constants.ENTITY_DOT + "creatorId");
        }
        Timestamp time = new Timestamp(System.currentTimeMillis());
        setFieldValByName("createTime", createTime != null ?
                createTime : time, metaObject);
        setFieldValByName("lastModifyTime", time, metaObject);
        if (creatorId != null) {
            return;
        }
//        if (AuthCurrentUser.getUser() != null) {
//            setFieldValByName("creatorId", AuthCurrentUser.getUserId(), metaObject);
//        } else {
//            setFieldValByName("creatorId", 0L, metaObject);
//        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("lastModifyTime", new Timestamp(System.currentTimeMillis()), metaObject);
        Long lastModifierId = null;
        if (metaObject.getOriginalObject() instanceof BaseEntity) {
            lastModifierId = ((BaseEntity) metaObject.getOriginalObject()).getUpdatorId();
        } else if (metaObject.getOriginalObject() instanceof MapperMethod.ParamMap) {
            lastModifierId = (Long) metaObject.getValue(Constants.ENTITY_DOT + "lastModifierId");
        }

        if (lastModifierId != null) {
            return;
        }
//        if (AuthCurrentUser.getUser() != null) {
//            setFieldValByName("lastModifierId", AuthCurrentUser.getUserId(), metaObject);
//        }
    }
}
