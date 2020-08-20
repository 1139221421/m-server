package com.lxl.common.feign.storage;

import com.lxl.common.vo.ResponseInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "storage")
public interface StorageFeign {

    @RequestMapping(value = "/sku/reduceStock", method = RequestMethod.GET)
    ResponseInfo reduceStock(@RequestParam("id") Long id, @RequestParam("num") Integer num);

}
