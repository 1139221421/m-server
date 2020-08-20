package com.lxl.storage.service;

import com.lxl.common.vo.ResponseInfo;

public interface SkuService {

    ResponseInfo reduceStock(Long id, Integer num);
}
