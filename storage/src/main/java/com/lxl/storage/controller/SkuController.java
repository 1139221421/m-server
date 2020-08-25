package com.lxl.storage.controller;

import com.lxl.common.entity.storage.Sku;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.storage.service.ISkuService;
import com.lxl.web.support.BaseCrudController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/sku")
public class SkuController extends BaseCrudController<Sku, ISkuService, Long> {
    @Autowired
    private ISkuService skuService;

    @Override
    protected ISkuService getService() {
        return skuService;
    }

    @ResponseBody
    @GetMapping("/reduceStock")
    public ResponseInfo reduceStock(@RequestParam("id") Long id, @RequestParam("num") Integer num) {
        return skuService.reduceStock(id, num);
    }

}
