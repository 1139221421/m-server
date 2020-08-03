package com.lxl.web.mq;

import com.lxl.common.enums.MqTagsEnum;
import com.lxl.utils.common.SpringContextUtils;
import com.lxl.utils.config.ConfUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * rocketmq 消息队列实现
 */
@SuppressWarnings("Duplicates")
@Component
public class RocketMqConsumer implements TransactionListener {

    /**
     * 消费组
     */
    private static final String CONSUMER_GROUP_NAME = ConfUtil.getPropertyOrDefault("rocketmq-consumer", "lxl-consumer");
    private static final String BROADCASY_GROUP_NAME = ConfUtil.getPropertyOrDefault("rocketmq-consumer", "lxl-consumer") + "-broadcast";
    /**
     * 生产者组
     */
    private static final String PRODUCER_GROUP_NAME = ConfUtil.getPropertyOrDefault("rocketmq-producer", "lxl-producer");
    /**
     * 主题topic
     */
    private static final String TOPIC_NAME = ConfUtil.getPropertyOrDefault("rocketmq-topic", "lxl");
    private static final String BROADCASY_TOPIC_NAME = ConfUtil.getPropertyOrDefault("rocketmq-topic", "lxl") + "-broadcast";
    /**
     * namesrv地址
     */
    private static final String NAMES_ADDR = ConfUtil.getPropertyOrDefault("rocketmq-addr", "dev::9876");

    private final static Logger logger = LoggerFactory.getLogger(RocketMqConsumer.class);
    private static final TransactionMQProducer PRODUCER = new TransactionMQProducer(PRODUCER_GROUP_NAME);
    private static final DefaultMQPushConsumer CONSUMER = new DefaultMQPushConsumer(CONSUMER_GROUP_NAME);
    private static final DefaultMQPushConsumer BROADCAST_CONSUMER = new DefaultMQPushConsumer(BROADCASY_GROUP_NAME);

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        logger.info("rocketmq starting ...");
        //启动消费者 添加消费者监听
        CONSUMER.setNamesrvAddr(NAMES_ADDR);
        CONSUMER.subscribe(TOPIC_NAME, "*");
        CONSUMER.setMessageListener((MessageListenerConcurrently) this::consumeMessage);
        CONSUMER.start();

        //启动广播消费者
        BROADCAST_CONSUMER.setNamesrvAddr(NAMES_ADDR);
        BROADCAST_CONSUMER.setMessageModel(MessageModel.BROADCASTING);
        BROADCAST_CONSUMER.subscribe(BROADCASY_TOPIC_NAME, "*");
        BROADCAST_CONSUMER.setMessageListener((MessageListenerConcurrently) this::consumeMessage);
        BROADCAST_CONSUMER.start();

        //启动生产者 启动事务监听
        PRODUCER.setNamesrvAddr(NAMES_ADDR);
        PRODUCER.setTransactionListener(new RocketMqConsumer());
        PRODUCER.start();
        logger.info("rocketmq started ...");
    }

    /**
     * 发送消息，此时对消费是不可见的（返回LocalTransactionState.COMMIT_MESSAGE才可见）
     *
     * @param msg
     * @param tag
     * @param isDefault      是否普通消息 true：普通消息  false：事务消息
     * @param delayTimeLevel 默认1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * @param isBroadcast    是否广播模式 true：广播
     * @return
     */
    public boolean sendMsg(String msg, MqTagsEnum tag, boolean isDefault, Integer delayTimeLevel, boolean isBroadcast) {
        try {
            String topicName = TOPIC_NAME;
            if (isBroadcast) {
                topicName = BROADCASY_TOPIC_NAME;
            }
            logger.info("发送消息信息：{},tag：{}", msg, tag);
            Message message = new Message(topicName, tag.getTagName(), msg.getBytes(RemotingHelper.DEFAULT_CHARSET));
            if (isDefault) {
                // 普通消息
                if (delayTimeLevel != null) {
                    // 延时消息
                    message.setDelayTimeLevel(delayTimeLevel);
                }
                PRODUCER.send(message, 3000);
            } else {
                // 事务消息
                PRODUCER.sendMessageInTransaction(message, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 普通消息
     *
     * @param msg
     * @param tag
     * @return
     */
    public boolean sendMsg(String msg, MqTagsEnum tag) {
        return sendMsg(msg, tag, true, null, false);
    }

    /**
     * 普通延时消息
     *
     * @param msg
     * @param tag
     * @param delayTimeLevel 默认1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * @return
     */
    public boolean sendMsg(String msg, MqTagsEnum tag, Integer delayTimeLevel) {
        return sendMsg(msg, tag, true, delayTimeLevel, false);
    }

    /**
     * 事务消息
     *
     * @param msg
     * @param tag
     * @return
     */
    public boolean sendTransactionMsg(String msg, MqTagsEnum tag) {
        return sendMsg(msg, tag, false, null, false);
    }

    /**
     * 广播
     *
     * @param msg
     * @param tag
     * @return
     */
    public boolean sendBroadcastMsg(String msg, MqTagsEnum tag) {
        return sendMsg(msg, tag, true, null, true);
    }

    /**
     * 消费消息
     *
     * @param msgs
     * @param context
     * @return
     */
    private ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            if (!msgs.isEmpty()) {
                for (MessageExt msg : msgs) {
                    //todo 消息重复消费验证。。。

                    String s = new String(msg.getBody());
                    logger.info("取得消费信息为：{},context:{}", s, context);
                    List<ConsumerDeal> beans = SpringContextUtils.getBeans(ConsumerDeal.class);
                    for (ConsumerDeal bean : beans) {
                        if (bean.supportTag(msg.getTags()) && !bean.deal(s)) {
                            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                        }
                    }
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            logger.error("消息消费出现异常，", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    /**
     * 执行本地事务 如果数据操作失败，需要回滚
     *
     * @param msg
     * @param arg
     * @return
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            // 业务操作
            String s = new String(msg.getBody());
            logger.info("执行本地事务取得信息为：{},", s);
            List<ProducerDeal> beans = SpringContextUtils.getBeans(ProducerDeal.class);
            for (ProducerDeal bean : beans) {
                if (bean.supportTag(msg.getTags()) && bean.excute(s)) {
                    logger.info("本地事务执行成功，发送【确认】消息:{}", s);
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
            }
            logger.info("本地事务执行失败，发送【回滚】消息:{}", s);
        } catch (Exception e) {
            logger.error("本地事务执行异常，", e);
        }
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }

    /**
     * 由于RocketMQ迟迟没有收到消息的确认消息，因此主动询问这条prepare消息，是否正常？
     * 可以查询数据库看这条数据是否已经处理,返回 COMMIT_MESSAGE 或者ROLLBACK_MESSAGE
     *
     * @param msg
     * @return
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        try {
            List<ProducerDeal> beans = SpringContextUtils.getBeans(ProducerDeal.class);
            String s = new String(msg.getBody());
            logger.info("mq回查事务消息:{}", s);
            for (ProducerDeal bean : beans) {
                if (bean.supportTag(msg.getTags()) && bean.check(s)) {
                    logger.info("mq回查事务消息，发送【确认】消息:{}", s);
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
            }
            logger.info("mq回查事务消息，发送【回滚】消息:{}", s);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("mq回查事务消息，", e);
        }
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }

}
