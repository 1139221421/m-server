package com.lxl.order.controller;

import com.lxl.common.vo.ResponseInfo;
import com.lxl.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/atCreateOrder")
    public ResponseInfo atCreateOrder() {
        return orderService.atCreateOrder();
    }

}
