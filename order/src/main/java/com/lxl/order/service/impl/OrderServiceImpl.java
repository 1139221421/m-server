package com.lxl.order.service.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lxl.common.entity.order.Order;
import com.lxl.common.enums.MqTagsEnum;
import com.lxl.common.feign.auth.AuthFeign;
import com.lxl.common.feign.storage.StorageFeign;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.order.dao.OrderMapper;
import com.lxl.order.service.IOrderService;
import com.lxl.order.service.ITccOrderService;
import com.lxl.web.mq.ProducerDeal;
import com.lxl.web.mq.RocketMqConsumer;
import com.lxl.web.support.CrudServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Service
public class OrderServiceImpl extends CrudServiceImpl<OrderMapper, Order, Long> implements IOrderService, ProducerDeal {

    @Resource
    private AuthFeign authFeign;

    @Resource
    private StorageFeign storageFeign;

    @Resource
    private ITccOrderService tccCreateOrderPrepare;

    @Resource
    private RocketMqConsumer rocketMqConsumer;

    /**
     * rocketmq最终事务一致性 模拟下单
     *
     * @return
     */
    @Override
    public ResponseInfo mqCreateOrder() {
        // mq扣款,（mq事务不好处理多个服务）
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("skuId", 1L);
        jsonObject.put("orderNum", UUID.fastUUID().toString(true));
        jsonObject.put("reduceStock", 1);
        rocketMqConsumer.sendTransactionMsg(JSON.toJSONString(jsonObject), MqTagsEnum.REDUCE_STOCK);
        return ResponseInfo.createSuccess();
    }

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
        order.setOrderNum(UUID.fastUUID().toString(true));
        order.setOrderPrice(new BigDecimal(10));
        save(order);

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
     * 注意：
     * 空回滚：Try未执行，Cancel 执行了
     * 幂等：多次调用方法（Confirm）
     * 悬挂：Cancel接口 比 Try接口先执行
     *
     * @return
     */
    @Override
    @GlobalTransactional
    public ResponseInfo tccCreateOrder() {
        Order order = new Order();
        order.setOrderName("分布式事务seata-tcc模拟下单");
        order.setOrderNum(UUID.fastUUID().toString(true));
        order.setOrderPrice(new BigDecimal(10));
        if (!tccCreateOrderPrepare.tccCreateOrderPrepare(order)) {
            throw new RuntimeException("分布式事务seata-tcc模拟下单准备失败");
        }
        return ResponseInfo.createSuccess();
//        ResponseInfo responseInfo = authFeign.tccReduceAccountBalancePrepare(1L, new BigDecimal(10L));
//        if (!responseInfo.getSuccess()) {
//            // 余额（检查和资源预留）
//            throw new RuntimeException("分布式事务seata-tcc模拟下单：" + responseInfo.getMessage());
//        }
//        responseInfo = storageFeign.tccReduceStockPrepare(1L, 1);
//        if (!responseInfo.getSuccess()) {
//            // 库存（检查和资源预留）
//            throw new RuntimeException("分布式事务seata-tcc模拟下单：" + responseInfo.getMessage());
//        }
//        responseInfo.setBusinessData(order);
//        return responseInfo;
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
