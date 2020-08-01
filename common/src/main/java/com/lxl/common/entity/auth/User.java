package com.lxl.common.entity.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lxl.common.entity.BaseEntity;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@TableName(value = "user")
@Document(indexName = "user#{@indexSuffix}")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String salt;
    private Integer age;
}
