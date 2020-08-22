package com.lxl.storage.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lxl.common.enums.MqTagsEnum;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.storage.dao.SkuMapper;
import com.lxl.storage.service.SkuService;
import com.lxl.web.mq.ConsumerDeal;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SkuServiceImpl implements SkuService, ConsumerDeal {
    @Resource
    private SkuMapper skuMapper;

    /**
     * 减库存
     *
     * @param id
     * @param num
     * @return
     */
    @Override
    public ResponseInfo reduceStock(Long id, Integer num) {
        skuMapper.reduceStock(id, num);
        return ResponseInfo.createSuccess();
    }

    @Override
    public boolean supportTag(String tagsEnum) {
        return tagsEnum.equals(MqTagsEnum.REDUCE_STOCK.getTagName());
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
}
