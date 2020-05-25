package com.lxl.web.mq;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消费mq消息
 *
 * @author
 */
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public interface ConsumerDeal {

    /**
     * 是否适配该tag信息
     *
     * @param tagsEnum
     * @return
     */
    boolean supportTag(String tagsEnum);

    /**
     * 消费者消费消息
     *
     * @param msg
     * @return
     */
    boolean deal(String msg);

}
