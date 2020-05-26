package com.lxl.message.controller;

import com.lxl.common.enums.MqTagsEnum;
import com.lxl.web.mq.ConsumerDeal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
public class MessageController implements ConsumerDeal {
    private final static Logger logger = LoggerFactory.getLogger(MessageController.class);

    @GetMapping("/test")
    private String test() {
        logger.info("messaage 调用...");
        return "message";
    }

    @Override
    public boolean supportTag(String tagsEnum) {
        return MqTagsEnum.TEST.getTagName().equals(tagsEnum);
    }

    @Override
    public boolean deal(String msg) {
        logger.info("成功消费mq消息...");
        return true;
    }
}
