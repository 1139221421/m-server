package com.lxl.web.mq;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import com.lxl.common.enums.MqTagsEnum;
import com.lxl.utils.common.SpringContextUtils;
import com.lxl.utils.config.ConfUtil;
import com.lxl.web.redis.RedisCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * rocketmq 消息队列实现
 */
@Slf4j
@Component
@SuppressWarnings("Duplicates")
@ConditionalOnBean(RedisCacheUtils.class)
public class RocketMqConsumer implements TransactionListener {

    @Autowired
    private ScheduledThreadPoolExecutor executor;

    @Autowired
    private RedisCacheUtils redisCacheUtils;

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

    private static final TransactionMQProducer PRODUCER = new TransactionMQProducer(PRODUCER_GROUP_NAME);
    private static final DefaultMQPushConsumer CONSUMER = new DefaultMQPushConsumer(CONSUMER_GROUP_NAME);
    private static final DefaultMQPushConsumer BROADCAST_CONSUMER = new DefaultMQPushConsumer(BROADCASY_GROUP_NAME);
    private static final String MQ_CONSUME_CACHE = "mq_consume_cache_";
    private static final String MSG_ID = "msg_id:";

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        log.info("rocketmq starting ...");
        //启动消费者 添加消费者监听
        CONSUMER.setNamesrvAddr(NAMES_ADDR);
        //一次消费失10条，默认1条
        CONSUMER.setConsumeMessageBatchMaxSize(10);
        CONSUMER.subscribe(TOPIC_NAME, "*");
        // 第一次启动从指定时间点开始消费,后续再启动接着上次消费的进度开始消费
        //指定时间点设置 setConsumeTimestamp() 默认值 UtilAll.timeMillisToHumanString3(System.currentTimeMillis() - (1000 * 60 * 30)) 半小时
        CONSUMER.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP);
        CONSUMER.setMessageListener((MessageListenerConcurrently) this::consumeMessage);
        CONSUMER.start();

        //启动广播消费者
        BROADCAST_CONSUMER.setNamesrvAddr(NAMES_ADDR);
        BROADCAST_CONSUMER.setConsumeMessageBatchMaxSize(10);
        BROADCAST_CONSUMER.setMessageModel(MessageModel.BROADCASTING);
        BROADCAST_CONSUMER.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP);
        BROADCAST_CONSUMER.subscribe(BROADCASY_TOPIC_NAME, "*");
        BROADCAST_CONSUMER.setMessageListener((MessageListenerConcurrently) this::consumeMessage);
        BROADCAST_CONSUMER.start();

        //启动生产者 启动事务监听
        PRODUCER.setNamesrvAddr(NAMES_ADDR);
        PRODUCER.setTransactionListener(new RocketMqConsumer());
        PRODUCER.start();
        log.info("rocketmq started ...");
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
    public void sendMsg(String msg, MqTagsEnum tag, boolean isDefault, Integer delayTimeLevel, boolean isBroadcast) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        if (!msg.startsWith(MSG_ID)) {
            // 添加消息标识，避免由于网络或者其他原因catch后重复发送
            msg = MSG_ID + UUID.fastUUID().toString(true) + "-" + msg;
        }
        try {
            String topicName = TOPIC_NAME;
            if (isBroadcast) {
                topicName = BROADCASY_TOPIC_NAME;
            }
            log.info("发送消息信息：{},tag：{}", msg, tag);
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
            int interval = Integer.parseInt(ConfUtil.getPropertyOrDefault("mq_retry_interval", "60"));
            log.warn("发送mq消息异常：{}，{}秒后重试间隔", e, interval);
            String msg1 = msg;
            executor.schedule(() -> {
                sendMsg(msg1, tag, isDefault, delayTimeLevel, isBroadcast);
            }, interval, TimeUnit.SECONDS);
        }
    }

    /**
     * 普通消息
     *
     * @param msg
     * @param tag
     * @return
     */
    public void sendMsg(String msg, MqTagsEnum tag) {
        sendMsg(msg, tag, true, null, false);
    }

    /**
     * 普通延时消息
     *
     * @param msg
     * @param tag
     * @param delayTimeLevel 默认1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * @return
     */
    public void sendMsg(String msg, MqTagsEnum tag, Integer delayTimeLevel) {
        sendMsg(msg, tag, true, delayTimeLevel, false);
    }

    /**
     * 事务消息
     *
     * @param msg
     * @param tag
     * @return
     */
    public void sendTransactionMsg(String msg, MqTagsEnum tag) {
        sendMsg(msg, tag, false, null, false);
    }

    /**
     * 广播
     *
     * @param msg
     * @param tag
     * @return
     */
    public void sendBroadcastMsg(String msg, MqTagsEnum tag) {
        sendMsg(msg, tag, true, null, true);
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
            if (CollectionUtil.isNotEmpty(msgs)) {
                int index;
                String message, msgId, s;
                for (MessageExt msg : msgs) {
                    message = new String(msg.getBody());
                    index = message.indexOf("-");
                    if (index <= 0) {
                        log.warn("检测到消息格式不正确：{}", message);
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    msgId = message.substring(0, index);
                    s = message.substring(index + 1);
                    if (redisCacheUtils.exist(MQ_CONSUME_CACHE + msgId)) {
                        log.warn("检测到有重复消息：{}", s);
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    log.info("取得消费信息为：{},context:{}", s, context);
                    List<ConsumerDeal> beans = SpringContextUtils.getBeans(ConsumerDeal.class);
                    for (ConsumerDeal bean : beans) {
                        if (bean.supportTag(msg.getTags())) {
                            if (bean.deal(s)) {
                                redisCacheUtils.setCacheObject(MQ_CONSUME_CACHE + msgId, true, 60 * 60 * 12);
                            } else {
                                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                            }
                            break;
                        }
                    }
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("消息消费出现异常，", e);
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
            log.info("执行本地事务取得信息为：{},", s);
            List<ProducerDeal> beans = SpringContextUtils.getBeans(ProducerDeal.class);
            for (ProducerDeal bean : beans) {
                if (bean.supportTag(msg.getTags()) && bean.excute(s)) {
                    log.info("本地事务执行成功，发送【确认】消息:{}", s);
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
            }
            log.info("本地事务执行失败，发送【回滚】消息:{}", s);
        } catch (Exception e) {
            log.error("本地事务执行异常，", e);
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
            log.info("mq回查事务消息:{}", s);
            for (ProducerDeal bean : beans) {
                if (bean.supportTag(msg.getTags()) && bean.check(s)) {
                    log.info("mq回查事务消息，发送【确认】消息:{}", s);
                    return LocalTransactionState.COMMIT_MESSAGE;
                }
            }
            log.info("mq回查事务消息，发送【回滚】消息:{}", s);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("mq回查事务消息，", e);
        }
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }

}
