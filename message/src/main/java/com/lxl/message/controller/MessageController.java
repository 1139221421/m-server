package com.lxl.message.controller;

import com.lxl.common.enums.MqTagsEnum;
import com.lxl.web.mq.ConsumerDeal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
public class MessageController implements ConsumerDeal {

    @GetMapping("/test")
    private String test() {
        System.out.println("messaage 调用...");
        return "message";
    }

    @Override
    public boolean supportTag(String tagsEnum) {
        return MqTagsEnum.TEST.getTagName().equals(tagsEnum);
    }

    @Override
    public boolean deal(String msg) {
        return true;
    }
}
