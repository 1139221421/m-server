package com.lxl.order.service;

import com.lxl.common.entity.order.Order;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.support.ICrudService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

public interface IOrderService extends ICrudService<Order, Long> {

    ResponseInfo mqCreateOrder();

    ResponseInfo atCreateOrder();

    ResponseInfo tccCreateOrder();

    /**
     * 检查和资源预留
     *
     * @param actionContext
     * @param order
     * @return
     */
    @TwoPhaseBusinessAction(name = "create_order", commitMethod = "tccCreateOrderCommit", rollbackMethod = "tccCreateOrderRollback")
    boolean tccCreateOrderPrepare(BusinessActionContext actionContext,
                                  @BusinessActionContextParameter(paramName = "order") Order order);

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

    ResponseInfo sagaCreateOrder();

}
