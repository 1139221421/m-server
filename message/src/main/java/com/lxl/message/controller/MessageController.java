package com.lxl.message.controller;

import com.lxl.common.entity.message.Message;
import com.lxl.common.enums.MqTagsEnum;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.message.service.MessageService;
import com.lxl.web.mq.ConsumerDeal;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController implements ConsumerDeal {
    private final static Logger logger = LoggerFactory.getLogger(MessageController.class);
    @Autowired
    private MessageService messageService;

    @GetMapping("/test")
    private ResponseInfo test() {
        logger.info("messaage 调用...");
        return ResponseInfo.createSuccess("success");
    }

    @PostMapping("/create")
    public ResponseInfo create(@RequestBody Message message) {
        messageService.create(message);
        return ResponseInfo.createSuccess();
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

    @PostMapping("/savePrepare")
    public ResponseInfo savePrepare(@RequestBody Message message) {
        return ResponseInfo.createSuccess().setSuccess(messageService.savePrepare(new BusinessActionContext(), message));
    }
}
