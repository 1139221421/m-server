package com.lxl.storage.service.impl;

import com.lxl.common.vo.ResponseInfo;
import com.lxl.storage.dao.SkuMapper;
import com.lxl.storage.service.SkuService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SkuServiceImpl implements SkuService {
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
}
