package com.lxl.order.service;

import com.lxl.common.vo.ResponseInfo;

public interface OrderService {

    ResponseInfo atCreateOrder();

    ResponseInfo tccCreateOrder();

    ResponseInfo sagaCreateOrder();

    ResponseInfo mqCreateOrder();

}
