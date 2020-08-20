package com.lxl.common.entity.order;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order")
public class Order {
    @TableField("order_name")
    private String orderName;
    @TableField("order_price")
    private BigDecimal orderPrice;
}
