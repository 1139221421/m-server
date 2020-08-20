package com.lxl.common.feign.auth;

import com.lxl.common.vo.ResponseInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "auth")
public interface AuthFeign {

    @RequestMapping(value = "/user/reduceAccountBalance", method = RequestMethod.GET)
    ResponseInfo reduceAccountBalance(@RequestParam("id") Long id, @RequestParam("reduce") BigDecimal reduce);

}
