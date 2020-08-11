package com.lxl.common.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 实体基类
 * @Aythor
 */
@Data
public class BaseEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = -34115333603863619L;
    /**
     * 主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    protected Long id;
    /**
     * 创建人
     */
    @TableField(value = "creator_id", fill = FieldFill.INSERT)
    private Long creatorId;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE, update = "now()")
    private Date updateTime;
    /**
     * 修改人
     */
    @TableField(value = "updator_id", fill = FieldFill.INSERT_UPDATE)
    private Long updatorId;

}
