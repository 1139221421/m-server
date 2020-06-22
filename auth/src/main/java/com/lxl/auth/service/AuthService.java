package com.lxl.auth.service;

import com.lxl.auth.vo.LoginRequestInfo;
import com.lxl.common.vo.ResponseInfo;

public interface AuthService {
    ResponseInfo login(LoginRequestInfo loginRequestInfo);
}
