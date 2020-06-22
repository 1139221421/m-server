package com.lxl.auth.service.impl;

import com.lxl.auth.service.AuthService;
import com.lxl.auth.service.UserService;
import com.lxl.auth.vo.LoginRequestInfo;
import com.lxl.auth.vo.LoginUserInfo;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.support.OperatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private OperatorUtils operatorUtils;

    @Override
    public ResponseInfo login(LoginRequestInfo loginRequestInfo) {
        ResponseInfo responseInfo = new ResponseInfo(false);
        LoginUserInfo loginUserInfo = userService.veryfiyUser(loginRequestInfo);
        if (loginUserInfo.getM() == null || !loginUserInfo.isSuccess()) {
            responseInfo.setMessage("用户名或者密码错误");
            return responseInfo;
        }
        String token = operatorUtils.createToken(loginUserInfo);
        responseInfo.addData("token", token);
        responseInfo.addData("userData", loginUserInfo.getM());
        responseInfo.addData("menuData", null);
        responseInfo.addData("permsData", null);
        responseInfo.setSuccess(true);
        return responseInfo;
    }
}
