package com.lxl.storage.service;

import com.lxl.common.entity.order.Order;
import com.lxl.common.entity.storage.Sku;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.support.ICrudService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

public interface ISkuService extends ICrudService<Sku, Long> {

    ResponseInfo reduceStock(Long id, Integer num);

    /**
     * 检查和资源预留
     *
     * @param actionContext
     * @return
     */
    @TwoPhaseBusinessAction(name = "create_order", commitMethod = "tccReduceStockCommit", rollbackMethod = "tccReduceStockRollback")
    ResponseInfo tccReduceStockPrepare(BusinessActionContext actionContext,
                                  @BusinessActionContextParameter(paramName = "id") Long id,
                                  @BusinessActionContextParameter(paramName = "num") Integer num);

    /**
     * 提交事务
     *
     * @param actionContext
     * @return
     */
    boolean tccReduceStockCommit(BusinessActionContext actionContext);

    /**
     * 取消回滚
     *
     * @param actionContext
     * @return
     */
    boolean tccReduceStockRollback(BusinessActionContext actionContext);
}
