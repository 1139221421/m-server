package com.lxl.auth.vo;

import lombok.Data;

@Data
public class LoginRequestInfo {
    private String username;
    private String password;
    private String captcha;
    private Integer loginType;
}
