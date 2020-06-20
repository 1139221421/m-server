package com.lxl.common.entity.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lxl.common.entity.BaseEntity;
import lombok.Data;

@Data
@TableName(value = "user")
public class User extends BaseEntity {
    private String name;

    private Integer age;
}
