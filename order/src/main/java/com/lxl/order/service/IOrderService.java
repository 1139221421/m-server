package com.lxl.order.service;

import com.lxl.common.entity.order.Order;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.support.ICrudService;

public interface IOrderService extends ICrudService<Order, Long> {

    ResponseInfo mqCreateOrder();

    ResponseInfo atCreateOrder();

    ResponseInfo tccCreateOrder();

    ResponseInfo sagaCreateOrder();

}
