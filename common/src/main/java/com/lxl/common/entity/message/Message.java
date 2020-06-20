package com.lxl.common.entity.message;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lxl.common.entity.BaseEntity;
import lombok.Data;

@Data
@TableName("message")
public class Message extends BaseEntity {
    private String title;
}
