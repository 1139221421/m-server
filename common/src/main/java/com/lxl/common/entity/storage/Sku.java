package com.lxl.common.entity.storage;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lxl.common.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("sku")
public class Sku extends BaseEntity {
    @TableField("sku_code")
    private String skuCode;
    @TableField("sku_name")
    private String skuName;
    @TableField("stock")
    private Integer stock;
    @TableField("price")
    private BigDecimal price;
}
