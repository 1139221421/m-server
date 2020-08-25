package com.lxl.order.controller;

import com.lxl.common.entity.order.Order;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.order.service.IOrderService;
import com.lxl.web.support.BaseCrudController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/order")
public class OrderController extends BaseCrudController<Order, IOrderService, Long> {
    @Autowired
    private IOrderService orderService;

    @Override
    protected IOrderService getService() {
        return orderService;
    }

    @ResponseBody
    @GetMapping("/mqCreateOrder")
    public ResponseInfo mqCreateOrder() {
        return orderService.mqCreateOrder();
    }

    @ResponseBody
    @GetMapping("/atCreateOrder")
    public ResponseInfo atCreateOrder() {
        return orderService.atCreateOrder();
    }

    @ResponseBody
    @GetMapping("/tccCreateOrder")
    public ResponseInfo tccCreateOrder() {
        return orderService.tccCreateOrder();
    }

}
