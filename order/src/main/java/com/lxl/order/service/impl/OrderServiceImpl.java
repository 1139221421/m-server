package com.lxl.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lxl.common.entity.order.Order;
import com.lxl.common.enums.MqTagsEnum;
import com.lxl.common.enums.TransactionEnum;
import com.lxl.order.dao.OrderMapper;
import com.lxl.order.service.IOrderService;
import com.lxl.web.annotations.TccVerify;
import com.lxl.web.mq.ProducerDeal;
import com.lxl.web.support.CrudServiceImpl;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class OrderServiceImpl extends CrudServiceImpl<OrderMapper, Order, Long> implements IOrderService, ProducerDeal {

    @Override
    public boolean supportTag(String tagsEnum) {
        return tagsEnum.equals(MqTagsEnum.REDUCE_STOCK.getTagName());
    }

    /**
     * 执行本地事务
     *
     * @param msg
     * @return
     */
    @Override
    public boolean excute(String msg) {
        // 消息已经投递但还不能消费，执行本地下单操作
        JSONObject jsonObject = JSON.parseObject(msg);
        Order order = new Order();
        order.setOrderNum(jsonObject.getString("orderNum"));
        order.setOrderName("分布式事务rocketmq模拟下单");
        order.setOrderPrice(new BigDecimal(10));
        save(order);
        // 返回true，通知mq可以消费消息,false表示回滚
        return true;
    }

    /**
     * 事务回查：检查本地事务是否正确执行
     *
     * @param msg
     * @return
     */
    @Override
    public boolean check(String msg) {
        //验证订单是否添加
        JSONObject jsonObject = JSON.parseObject(msg);
        return count(Wrappers.<Order>lambdaQuery().eq(Order::getOrderNum, jsonObject.getString("orderNum"))) > 0;
    }

    @Override
    @TccVerify(transaction = TransactionEnum.PREPARE)
    public boolean tccCreateOrderPrepare(Order order) {
        // 创建订单不需要准备什么
        log.info("分布式事务seata-tcc模拟下单，检查下单操作，xid：{}", RootContext.getXID());
        return true;
    }

    @Override
    @TccVerify(transaction = TransactionEnum.COMMIT)
    public boolean tccCreateOrderCommit(BusinessActionContext actionContext) {
        Order order = JSON.parseObject(JSON.toJSONString(actionContext.getActionContext("order")), Order.class);
        save(order);
        log.info("分布式事务seata-tcc模拟下单，提交下单操作，xid：{}", actionContext.getXid());
        return true;
    }

    @Override
    @TccVerify(transaction = TransactionEnum.ROLLBACK)
    public boolean tccCreateOrderRollback(BusinessActionContext actionContext) {
        log.info("分布式事务seata-tcc模拟下单失败，回滚下单操作，xid：{}", actionContext.getXid());
        return true;
    }

}
