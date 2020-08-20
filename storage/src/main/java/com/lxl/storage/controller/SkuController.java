package com.lxl.storage.controller;

import com.lxl.common.vo.ResponseInfo;
import com.lxl.storage.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sku")
public class SkuController {
    @Autowired
    private SkuService skuService;

    @GetMapping("/reduceStock")
    public ResponseInfo reduceStock(@RequestParam("id") Long id, @RequestParam("num") Integer num) {
        return skuService.reduceStock(id, num);
    }
}
