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

    /**
     * 熔断
     *
     * @return
     */
    @HystrixCommand(
            groupKey = "testGroup", // 一组 Hystrix 命令的集合， 用来统计、报告，默认取类名，可不配置
            commandKey = "testCommand", //用来标识一个 Hystrix 命令，默认会取被注解的方法名
            fallbackMethod = "fallback", // 超时调用方法
            commandProperties = { // 与此命令相关的属性
                    @HystrixProperty(name = "execution.timeout.enabled", value = "true"), // 是否给方法执行设置超时，默认为 true
                    @HystrixProperty(name = "execution.isolation.thread.interruptOnTimeout", value = "3000") // 方法执行超时时间，默认值是 1000，即 1秒
            }
    )
    @GetMapping("/test")
    public String test() {
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
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
