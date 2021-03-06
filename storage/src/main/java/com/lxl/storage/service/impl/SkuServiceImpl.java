package com.lxl.storage.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lxl.common.constance.Constance;
import com.lxl.common.entity.storage.Sku;
import com.lxl.common.enums.TagsEnum;
import com.lxl.common.enums.TransactionEnum;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.storage.dao.SkuMapper;
import com.lxl.storage.service.ISkuService;
import com.lxl.web.annotations.DisLockDeal;
import com.lxl.web.annotations.TccVerify;
import com.lxl.web.mq.ConsumerDeal;
import com.lxl.web.support.CrudServiceImpl;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SkuServiceImpl extends CrudServiceImpl<SkuMapper, Sku, Long> implements ISkuService, ConsumerDeal {

    /**
     * 减库存
     *
     * @param id
     * @param num
     * @return
     */
    @Override
    public ResponseInfo reduceStock(Long id, Integer num) {
        baseMapper.reduceStock(id, num);
        return ResponseInfo.createSuccess();
    }

    /**
     * 库存校验和冻结(需要考虑并发)
     *
     * @param id
     * @param num
     * @return
     */
    @Override
    @TccVerify(transaction = TransactionEnum.PREPARE, lockName = TagsEnum.REDUCE_STOCK, lockId = "#p0")
    public boolean tccReduceStockPrepare(Long id, Integer num) {
        log.info("分布式事务seata-tcc模拟下单，检查库存操作，xid：{}", RootContext.getXID());
        Integer stock = redisCacheUtils.hGet(Constance.Storage.STOCK, id.toString(), Integer.class);
        if (stock == null) {
            // 初始化库存
            stock = initStock(id);
        }
        if (stock == null || stock < num) {
            // 检查库存是否充足
            throw new RuntimeException("库存不足");
        }
        // 库存冻结
        redisCacheUtils.hIncrBy(Constance.Storage.STOCK, id.toString(), -num);
        return true;
    }

    @Override
    @TccVerify(transaction = TransactionEnum.COMMIT)
    public boolean tccReduceStockCommit(BusinessActionContext actionContext) {
        Long id = Long.valueOf(actionContext.getActionContext("id").toString());
        Integer num = (Integer) actionContext.getActionContext("num");
        reduceStock(id, num);
        log.info("分布式事务seata-tcc模拟下单，提交库存操作，xid：{}", actionContext.getXid());
        return true;
    }

    @Override
    @TccVerify(transaction = TransactionEnum.ROLLBACK)
    public boolean tccReduceStockRollback(BusinessActionContext actionContext) {
        Long id = Long.valueOf(actionContext.getActionContext("id").toString());
        Integer num = (Integer) actionContext.getActionContext("num");
        redisCacheUtils.hIncrBy(Constance.Storage.STOCK, id.toString(), num);
        log.info("分布式事务seata-tcc模拟下单失败，回滚库存操作，xid：{}", actionContext.getXid());
        return true;
    }

    @Override
    public boolean supportTag(String tagsEnum) {
        return tagsEnum.equals(TagsEnum.REDUCE_STOCK.getTagName());
    }

    /**
     * mq减库存
     *
     * @param msg
     * @return
     */
    @Override
    public boolean deal(String msg) {
        JSONObject jsonObject = JSON.parseObject(msg);
        reduceStock(jsonObject.getLong("skuId"), jsonObject.getInteger("reduceStock"));
        return true;
    }

    /**
     * 初始化库存
     *
     * @param id
     */
    private Integer initStock(Long id) {
        Sku sku = findById(id);
        if (sku != null) {
            redisCacheUtils.hIncrBy(Constance.Storage.STOCK, id.toString(), sku.getStock());
            return sku.getStock();
        }
        return null;
    }
}
