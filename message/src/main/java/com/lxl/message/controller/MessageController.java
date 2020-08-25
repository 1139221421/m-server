package com.lxl.message.controller;

import com.lxl.common.entity.message.Message;
import com.lxl.message.service.IMessageService;
import com.lxl.web.support.BaseCrudController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/message")
public class MessageController extends BaseCrudController<Message, IMessageService, Long> {
    @Autowired
    private IMessageService messageService;

    @Override
    protected IMessageService getService() {
        return messageService;
    }
}
