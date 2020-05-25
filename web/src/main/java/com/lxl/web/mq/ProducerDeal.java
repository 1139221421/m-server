package com.lxl.web.mq;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 执行本地事务
 *
 * @author
 */
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public interface ProducerDeal {

    /**
     * 是否适配该tag信息
     *
     * @param tagsEnum
     * @return
     */
    boolean supportTag(String tagsEnum);

    /**
     * 生产者业务，执行本地事务
     *
     * @param msg
     * @return
     */
    boolean excute(String msg);

    /**
     * mq回查事务消息状态
     *
     * @param msg
     * @return
     */
    boolean check(String msg);

}
