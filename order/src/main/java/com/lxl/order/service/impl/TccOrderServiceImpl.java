package com.lxl.order.service.impl;

import com.lxl.common.entity.order.Order;
import com.lxl.order.service.ITccOrderService;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TccOrderServiceImpl implements ITccOrderService {

    @Override
    public boolean tccCreateOrderPrepare(Order order) {
        // 创建订单不需要准备什么
        log.info("分布式事务seata-tcc模拟下单，检查下单操作，xid：{}", RootContext.getXID());
        return true;
    }

    @Override
    public boolean tccCreateOrderCommit(BusinessActionContext actionContext) {
        log.info("分布式事务seata-tcc模拟下单，提交下单操作，xid：{}", actionContext.getXid());
        return true;
    }

    @Override
    public boolean tccCreateOrderRollback(BusinessActionContext actionContext) {
        log.info("分布式事务seata-tcc模拟下单失败，回滚下单操作，xid：{}", actionContext.getXid());
        return true;
    }

}
