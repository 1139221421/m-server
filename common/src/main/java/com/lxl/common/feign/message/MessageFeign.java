package com.lxl.common.feign.message;

import com.lxl.common.vo.ResponseInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "message")
public interface MessageFeign {
    @RequestMapping(value = "/message/test", method = RequestMethod.GET)
    ResponseInfo test();
}
