package com.lxl.message.service.impl;

import com.lxl.common.entity.message.Message;
import com.lxl.message.dao.MessageMapper;
import com.lxl.message.service.MessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    private MessageMapper messageMapper;

    @Override
    public void create(Message message) {
        messageMapper.insert(message);
    }
}
