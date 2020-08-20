package com.lxl.order.service.impl;

import com.lxl.order.dao.OrderMapper;
import com.lxl.order.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderMapper orderMapper;


}
