package com.lxl.order.controller;

import com.lxl.common.vo.ResponseInfo;
import com.lxl.order.service.IBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/business")
public class BusinessController {
    @Autowired
    private IBusinessService businessService;

    @ResponseBody
    @GetMapping("/mqCreateOrder")
    public ResponseInfo mqCreateOrder() {
        return businessService.mqCreateOrder();
    }

    @ResponseBody
    @GetMapping("/atCreateOrder")
    public ResponseInfo atCreateOrder() {
        return businessService.atCreateOrder();
    }

    @ResponseBody
    @GetMapping("/tccCreateOrder")
    public ResponseInfo tccCreateOrder() {
        return businessService.tccCreateOrder();
    }

    @ResponseBody
    @GetMapping("/sagaCreateOrder")
    public ResponseInfo sagaCreateOrder() {
        return businessService.sagaCreateOrder();
    }

}
