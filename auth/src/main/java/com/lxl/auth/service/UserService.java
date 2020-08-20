package com.lxl.auth.service;

import com.lxl.auth.vo.LoginRequestInfo;
import com.lxl.auth.vo.LoginUserInfo;
import com.lxl.common.entity.auth.User;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

import java.util.List;

public interface UserService {

    List<User> findAll();

    void crate(User user);

    void update(User user);

    void delete(Long id);

    LoginUserInfo veryfiyUser(LoginRequestInfo loginRequestInfo);

    void tccUpdate(User user);

    /**
     * 检查和资源预留
     *
     * @param actionContext
     * @param user
     * @return
     */
    @TwoPhaseBusinessAction(name = "userTccAction", commitMethod = "updateCommit", rollbackMethod = "updateRollback")
    boolean updatePrepare(BusinessActionContext actionContext,
                          @BusinessActionContextParameter(paramName = "user") User user);

    /**
     * 提交事务
     *
     * @param actionContext
     * @return
     */
    boolean updateCommit(BusinessActionContext actionContext);

    /**
     * 取消回滚
     *
     * @param actionContext
     * @return
     */
    boolean updateRollback(BusinessActionContext actionContext);
}
