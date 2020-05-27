package com.lxl.auth.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.lxl.common.enums.MqTagsEnum;
import com.lxl.common.feign.message.MessageFeign;
import com.lxl.web.mq.ProducerDeal;
import com.lxl.web.mq.RocketMqConsumer;
import com.lxl.web.utils.ExceptionUtil;
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
     * 服务器降级限流
     * value：资源名称
     * entryType：entry 类型，可选项（默认为 EntryType.OUT）
     * blockHandler / blockHandlerClass: blockHandler 函数 public，返回类型需要与原方法相匹配，参数类型需要和原方法相匹配并且最后加一个额外的参数，类型为 BlockException
     * 更多参数 https://github.com/alibaba/Sentinel/wiki/%E6%B3%A8%E8%A7%A3%E6%94%AF%E6%8C%81
     *
     * @return
     */
    @SentinelResource(value = "test", blockHandler = "handleException", blockHandlerClass = ExceptionUtil.class)
//    @SentinelResource(value = "test", blockHandler = "handleException")
    @GetMapping("/test")
    public String test() {
        logger.info("auth 访问...");
//        rocketMqConsumer.sendTransactionMsg("mq调用测试", MqTagsEnum.TEST);
        return messageFeign.test();
    }

    /**
     * blockHandlerClass没有时，走此处
     *
     * @param ex
     * @return
     */
    public String handleException(BlockException ex) {
        ex.printStackTrace();
        return "坚持不住了。。。";
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

