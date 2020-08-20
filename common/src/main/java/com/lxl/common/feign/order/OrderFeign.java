package com.lxl.common.feign.order;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "order")
public interface OrderFeign {

}
