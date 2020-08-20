package com.lxl.storage.service.impl;

import com.lxl.storage.dao.SkuMapper;
import com.lxl.storage.service.SkuService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SkuServiceImpl implements SkuService {
    @Resource
    private SkuMapper skuMapper;


}
