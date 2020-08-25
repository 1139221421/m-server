package com.lxl.auth.service;

import com.lxl.auth.vo.LoginRequestInfo;
import com.lxl.auth.vo.LoginUserInfo;
import com.lxl.common.entity.auth.User;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.support.ICrudService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

import java.math.BigDecimal;
import java.util.List;

public interface IUserService extends ICrudService<User, Long> {

    LoginUserInfo<User> veryfiyUser(LoginRequestInfo loginRequestInfo);

    ResponseInfo reduceAccountBalance(Long id, BigDecimal reduce);
}
