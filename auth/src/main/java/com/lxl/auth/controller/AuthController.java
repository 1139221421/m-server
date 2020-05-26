package com.lxl.auth.controller;

import com.lxl.common.enums.MqTagsEnum;
import com.lxl.common.feign.message.MessageFeign;
import com.lxl.web.mq.ProducerDeal;
import com.lxl.web.mq.RocketMqConsumer;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController implements ProducerDeal {
    private final static Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private MessageFeign messageFeign;

    @Autowired
    private RocketMqConsumer rocketMqConsumer;

    @GetMapping("/test")
    public String test() {
        logger.info("auth 访问...");
//        rocketMqConsumer.sendTransactionMsg("mq调用测试", MqTagsEnum.TEST);
        return messageFeign.test();
    }

    private String fallback() {
        return "调用超时";
    }

    @Override
    public boolean supportTag(String tagsEnum) {
        return MqTagsEnum.TEST.getTagName().equals(tagsEnum);
    }

    @Override
    public boolean excute(String msg) {
        return true;
    }

    @Override
    public boolean check(String msg) {
        // 模拟成功
        return true;
    }
}
