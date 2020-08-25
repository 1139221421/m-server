package com.lxl.message.service.impl;

import com.lxl.common.entity.message.Message;
import com.lxl.message.dao.MessageMapper;
import com.lxl.message.service.IMessageService;
import com.lxl.web.support.CrudServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends CrudServiceImpl<MessageMapper, Message, Long> implements IMessageService {

}
