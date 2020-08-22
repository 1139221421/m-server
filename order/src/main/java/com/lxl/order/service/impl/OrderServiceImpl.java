package com.lxl.order.service.impl;

import com.lxl.common.entity.order.Order;
import com.lxl.common.feign.auth.AuthFeign;
import com.lxl.common.feign.storage.StorageFeign;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.order.dao.OrderMapper;
import com.lxl.order.service.OrderService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderMapper orderMapper;

    @Resource
    private AuthFeign authFeign;

    @Resource
    private StorageFeign storageFeign;
    
    /**
     * rocketmq最终事务一致性 模拟下单
     *
     * @return
     */
    @Override
    public ResponseInfo mqCreateOrder() {
        return null;
    }

    /**
     * 分布式事务seata-at模拟下单
     *
     * @return
     */
    @Override
    @GlobalTransactional
    public ResponseInfo atCreateOrder() {
        // 下单
        Order order = new Order();
        order.setOrderName("分布式事务seata-at模拟下单");
        order.setOrderPrice(new BigDecimal(10));
        orderMapper.insert(order);

        // 扣款（未验证金额）
        ResponseInfo responseInfo = authFeign.reduceAccountBalance(1L, new BigDecimal(10));
        if (!responseInfo.getSuccess()) {
            throw new RuntimeException(responseInfo.getMessage());
        }

        // 减库存（未验证库存）
        responseInfo = storageFeign.reduceStock(1L, 1);
        if (!responseInfo.getSuccess()) {
            throw new RuntimeException(responseInfo.getMessage());
        }

        return ResponseInfo.createSuccess();
    }

    /**
     * 分布式事务seata-tcc模拟下单
     *
     * @return
     */
    @Override
    public ResponseInfo tccCreateOrder() {
        return null;
    }

    @Override
    public boolean tccCreateOrderPrepare(BusinessActionContext actionContext, Order order) {
        return false;
    }

    @Override
    public boolean tccCreateOrderCommit(BusinessActionContext actionContext) {
        return false;
    }

    @Override
    public boolean tccCreateOrderRollback(BusinessActionContext actionContext) {
        return false;
    }

    /**
     * 分布式事务seata-saga模拟下单
     *
     * @return
     */
    @Override
    public ResponseInfo sagaCreateOrder() {
        return null;
    }

}
