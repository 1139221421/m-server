package com.lxl.order.service;

import com.lxl.common.entity.order.Order;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

@LocalTCC
public interface ITccOrderService {

    /**
     * 检查和资源预留
     *
     * @param order
     * @return
     */
    @TwoPhaseBusinessAction(name = "create_order", commitMethod = "tccCreateOrderCommit", rollbackMethod = "tccCreateOrderRollback")
    boolean tccCreateOrderPrepare(@BusinessActionContextParameter(paramName = "order") Order order);

    /**
     * 提交事务
     *
     * @param actionContext
     * @return
     */
    boolean tccCreateOrderCommit(BusinessActionContext actionContext);

    /**
     * 取消回滚
     *
     * @param actionContext
     * @return
     */
    boolean tccCreateOrderRollback(BusinessActionContext actionContext);

}
