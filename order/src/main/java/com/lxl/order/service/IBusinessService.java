package com.lxl.order.service;

import com.lxl.common.vo.ResponseInfo;

public interface IBusinessService {

    ResponseInfo mqCreateOrder();

    ResponseInfo atCreateOrder();

    ResponseInfo tccCreateOrder();

    ResponseInfo sagaCreateOrder();

}
