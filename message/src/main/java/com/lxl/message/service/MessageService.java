package com.lxl.message.service;

import com.lxl.common.entity.message.Message;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

public interface MessageService {
    void create(Message message);

    /**
     * 检查和资源预留
     *
     * @param actionContext
     * @param message
     * @return
     */
    @TwoPhaseBusinessAction(name = "messageTccAction", commitMethod = "saveCommit", rollbackMethod = "saveRollback")
    boolean savePrepare(BusinessActionContext actionContext,
                        @BusinessActionContextParameter(paramName = "message") Message message);

    /**
     * 提交事务
     *
     * @param actionContext
     * @return
     */
    boolean saveCommit(BusinessActionContext actionContext);

    /**
     * 取消回滚
     *
     * @param actionContext
     * @return
     */
    boolean saveRollback(BusinessActionContext actionContext);

}
