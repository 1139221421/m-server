package com.lxl.storage.service;

import com.lxl.common.entity.storage.Sku;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.support.ICrudService;

public interface ISkuService extends ICrudService<Sku, Long> {

    ResponseInfo reduceStock(Long id, Integer num);
}
