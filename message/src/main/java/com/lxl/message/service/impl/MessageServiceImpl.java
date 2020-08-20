package com.lxl.message.service.impl;

import com.lxl.common.entity.auth.User;
import com.lxl.common.entity.message.Message;
import com.lxl.message.dao.MessageMapper;
import com.lxl.message.service.MessageService;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    private MessageMapper messageMapper;

    @Override
    public void create(Message message) {
        messageMapper.insert(message);
    }

    /**
     * 检查和资源预留
     *
     * @param actionContext
     * @param message
     * @return
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean savePrepare(BusinessActionContext actionContext, Message message) {
        return false;
    }

    /**
     * 提交事务
     * 幂等：多次调用方法（Confirm）
     *
     * @param actionContext
     * @return
     */
    @Override
    public boolean saveCommit(BusinessActionContext actionContext) {
        return false;
    }

    /**
     * 取消回滚
     * 空回滚：Try未执行，Cancel 执行了
     * 悬挂：Cancel接口 比 Try接口先执行
     *
     * @param actionContext
     * @return
     */
    @Override
    public boolean saveRollback(BusinessActionContext actionContext) {
        return false;
    }
}
