package com.lxl.common.entity.order;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lxl.common.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_info")
public class Order extends BaseEntity {
    @TableField("order_name")
    private String orderName;
    @TableField("order_price")
    private BigDecimal orderPrice;
}
