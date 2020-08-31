package com.lxl.order.service.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lxl.common.entity.order.Order;
import com.lxl.common.enums.TagsEnum;
import com.lxl.common.feign.auth.AuthFeign;
import com.lxl.common.feign.storage.StorageFeign;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.order.service.IBusinessService;
import com.lxl.order.service.IOrderService;
import com.lxl.web.mq.RocketMqConsumer;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Service
public class BusinessServiceImpl implements IBusinessService {

    @Resource
    private AuthFeign authFeign;

    @Resource
    private StorageFeign storageFeign;

    @Resource
    private IOrderService orderService;

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
        rocketMqConsumer.sendTransactionMsg(JSON.toJSONString(jsonObject), TagsEnum.REDUCE_STOCK);
        return ResponseInfo.createSuccess();
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
        orderService.save(order);

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
     * 分布式事务seata-tcc模拟下单(commit和rollback可用mq异步处理，提升响应时间)
     * 注意：
     * 空回滚：Try未执行，Cancel 执行了
     * 幂等：多次调用方法（Confirm）
     * 悬挂：Cancel接口 比 Try接口先执行
     * 步骤：
     * 1.接口层 类添加@LocalTCC注解，并实现prepare（添加@TwoPhaseBusinessAction注解，指定commit和rollback） commit rollback三个方法
     * 2.实现层 根据业务实现上面的三个方法
     * 3.业务层调用接口层的prepare方法，并添加@GlobalTransactional注解（多服务调用重复上述步骤即可）
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
        // @GlobalTransactional和@TwoPhaseBusinessAction在同一类问题：无法触发到父接口类的@TwoPhaseBusinessAction导致事务无法开启
        if (!orderService.tccCreateOrderPrepare(order)) {
            throw new RuntimeException("分布式事务seata-tcc模拟下单准备失败");
        }
        ResponseInfo responseInfo = authFeign.tccReduceAccountBalancePrepare(1L, new BigDecimal(10L));
        if (!responseInfo.getSuccess()) {
            // 余额（检查和资源预留）
            throw new RuntimeException("分布式事务seata-tcc模拟下单：" + responseInfo.getMessage());
        }
        responseInfo = storageFeign.tccReduceStockPrepare(1L, 1);
        if (!responseInfo.getSuccess()) {
            // 库存（检查和资源预留）
            throw new RuntimeException("分布式事务seata-tcc模拟下单：" + responseInfo.getMessage());
        }
        responseInfo.setBusinessData(order);
        return responseInfo;
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
