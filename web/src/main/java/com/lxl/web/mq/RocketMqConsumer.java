package com.lxl.web.mq;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.lxl.common.enums.MqTagsEnum;
import com.lxl.utils.common.SpringContextUtils;
import com.lxl.utils.config.ConfUtil;
import com.lxl.web.redis.RedisCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
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
    private static final String ORDER_GROUP_NAME = ConfUtil.getPropertyOrDefault("rocketmq-consumer", "lxl-consumer") + "-order";
    /**
     * 生产者组
     */
    private static final String PRODUCER_GROUP_NAME = ConfUtil.getPropertyOrDefault("rocketmq-producer", "lxl-producer");
    /**
     * 主题topic
     */
    private static final String TOPIC_NAME = ConfUtil.getPropertyOrDefault("rocketmq-topic", "lxl");
    private static final String BROADCASY_TOPIC_NAME = ConfUtil.getPropertyOrDefault("rocketmq-topic", "lxl") + "-broadcast";
    private static final String ORDER_TOPIC_NAME = ConfUtil.getPropertyOrDefault("rocketmq-topic", "lxl") + "-order";
    /**
     * namesrv地址
     */
    private static final String NAMES_ADDR = ConfUtil.getPropertyOrDefault("rocketmq-addr", "dev::9876");

    private static final TransactionMQProducer PRODUCER = new TransactionMQProducer(PRODUCER_GROUP_NAME);
    private static final DefaultMQPushConsumer CONSUMER = new DefaultMQPushConsumer(CONSUMER_GROUP_NAME);
    private static final DefaultMQPushConsumer BROADCAST_CONSUMER = new DefaultMQPushConsumer(BROADCASY_GROUP_NAME);
    private static final DefaultMQPushConsumer ORDER_CONSUMER = new DefaultMQPushConsumer(ORDER_GROUP_NAME);
    private static final String MQ_CONSUME_CACHE = "mq_consume_cache_";
    private static final String MQ_COMMIT_CACHE = "mq_commit_cache_";
    private static final String MSG_ID = "msg_id:";
    private static final String SEPARATOR = "-";

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

        //启动顺序消费者
        ORDER_CONSUMER.setNamesrvAddr(NAMES_ADDR);
        ORDER_CONSUMER.setConsumeMessageBatchMaxSize(1);
        ORDER_CONSUMER.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP);
        ORDER_CONSUMER.subscribe(ORDER_TOPIC_NAME, "*");
        ORDER_CONSUMER.setMessageListener((MessageListenerOrderly) this::orderConsumeMessage);
        ORDER_CONSUMER.start();

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
     * @param orderId        顺序消费：要保证同一个orderId任务的所有消息发送到同一个队列上，才能保证FIFO的顺序
     * @return
     */
    public void sendMsg(String msg, MqTagsEnum tag, boolean isDefault, Integer delayTimeLevel, boolean isBroadcast, String orderId) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        if (!msg.startsWith(MSG_ID)) {
            // 添加消息标识，避免由于网络或者其他原因catch后重复发送
            msg = MSG_ID + UUID.fastUUID().toString(true) + SEPARATOR + msg;
        }
        try {
            String topicName = TOPIC_NAME;
            if (isBroadcast) {
                topicName = BROADCASY_TOPIC_NAME;
            } else if (StringUtils.isNotEmpty(orderId)) {
                topicName = ORDER_TOPIC_NAME;
            }
            log.info("发送消息信息：{},tag：{}", msg, tag);
            Message message = new Message(topicName, tag.getTagName(), msg.getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult sendResult;
            if (isDefault) {
                // 普通消息
                if (delayTimeLevel != null) {
                    // 延时消息
                    message.setDelayTimeLevel(delayTimeLevel);
                }
                if (StringUtils.isEmpty(orderId)) {
                    sendResult = PRODUCER.send(message);
                } else {
                    // 发送到mq服务器同一个队列上，保证顺序
                    sendResult = PRODUCER.send(message, new MessageQueueSelector() {
                        @Override
                        public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                            String orderId = (String) arg;
                            return mqs.get(orderId.hashCode() % mqs.size());
                        }
                    }, orderId);
                }
            } else {
                // 事务消息
                sendResult = PRODUCER.sendMessageInTransaction(message, null);
            }
            // RocketMQ SendStatus状态说明及如何保证数据不丢失 https://blog.csdn.net/qq_39683476/article/details/87878753
            if (sendResult == null || sendResult.getSendStatus() != SendStatus.SEND_OK) {
                log.warn("消息发送异常,发送返回状态：{}", JSON.toJSONString(sendResult));
                reSendMsg(msg, tag, isDefault, delayTimeLevel, isBroadcast, orderId);
            }
        } catch (Exception e) {
            log.warn("发送mq消息异常：", e);
            reSendMsg(msg, tag, isDefault, delayTimeLevel, isBroadcast, orderId);
        }
    }

    /**
     * 消息发送异常，触发重发机制
     */
    private void reSendMsg(String msg, MqTagsEnum tag, boolean isDefault, Integer delayTimeLevel, boolean isBroadcast, String orderId) {
        int interval = Integer.parseInt(ConfUtil.getPropertyOrDefault("mq_retry_interval", "60"));
        executor.schedule(() -> {
            log.warn("消息发送异常，触发重发机制，消息内容：{}", msg);
            sendMsg(msg, tag, isDefault, delayTimeLevel, isBroadcast, orderId);
        }, interval, TimeUnit.SECONDS);
    }

    /**
     * 普通消息
     *
     * @param msg
     * @param tag
     * @return
     */
    public void sendMsg(String msg, MqTagsEnum tag) {
        sendMsg(msg, tag, true, null, false, null);
    }

    /**
     * 普通顺序消息
     *
     * @param msg
     * @param tag
     */
    public void sendOrderMsg(String msg, MqTagsEnum tag, String orderId) {
        sendMsg(msg, tag, true, null, false, orderId);
    }

    /**
     * 普通延时消息
     *
     * @param msg
     * @param tag
     * @param delayTimeLevel 默认1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * @return
     */
    public void sendDelayMsg(String msg, MqTagsEnum tag, Integer delayTimeLevel) {
        sendMsg(msg, tag, true, delayTimeLevel, false, null);
    }

    /**
     * 事务消息
     *
     * @param msg
     * @param tag
     * @return
     */
    public void sendTransactionMsg(String msg, MqTagsEnum tag) {
        sendMsg(msg, tag, false, null, false, null);
    }

    /**
     * 广播
     *
     * @param msg
     * @param tag
     * @return
     */
    public void sendBroadcastMsg(String msg, MqTagsEnum tag) {
        sendMsg(msg, tag, true, null, true, null);
    }

    /**
     * 消费消息
     *
     * @param msgs
     * @param context
     * @return
     */
    private ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        return consume(msgs) ? ConsumeConcurrentlyStatus.CONSUME_SUCCESS : ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }

    /**
     * 顺序消费
     *
     * @param msgs
     * @param context
     * @return
     */
    private ConsumeOrderlyStatus orderConsumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        // 当SUSPEND_CURRENT_QUEUE_A_MOMENT时（autoCommit设置无效），把消息从msgTreeMapTemp转移回msgTreeMap，等待下次消费
        return consume(msgs) ? ConsumeOrderlyStatus.SUCCESS : ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
    }

    /**
     * 消息消费具体处理流程
     *
     * @param msgs
     * @return
     */
    private boolean consume(List<MessageExt> msgs) {
        try {
            if (CollectionUtil.isNotEmpty(msgs)) {
                String message, msgId, s;
                for (MessageExt msg : msgs) {
                    message = new String(msg.getBody());
                    msgId = getMsgId(message);
                    s = getRealMsg(message);
                    if (!redisCacheUtils.exist(MQ_CONSUME_CACHE + msgId)) {
                        if (s == null) {
                            continue;
                        }
                        log.info("取得消费信息为：{}", s);
                        List<ConsumerDeal> beans = SpringContextUtils.getBeans(ConsumerDeal.class);
                        for (ConsumerDeal bean : beans) {
                            if (bean.supportTag(msg.getTags())) {
                                if (bean.deal(s)) {
                                    redisCacheUtils.setCacheObject(MQ_CONSUME_CACHE + msgId, true, 60 * 60 * 12);
                                } else {
                                    return false;
                                }
                                break;
                            }
                        }
                    } else {
                        log.warn("检测到有重复消息：{}", s);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error("消息消费出现异常，", e);
            return false;
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
            String msgId = getMsgId(s);
            if (redisCacheUtils.exist(MQ_COMMIT_CACHE + msgId)) {
                log.info("mq重复执行本地事务:{}", s);
                return LocalTransactionState.COMMIT_MESSAGE;
            }
            log.info("执行本地事务取得信息为：{},", s);
            List<ProducerDeal> beans = SpringContextUtils.getBeans(ProducerDeal.class);
            for (ProducerDeal bean : beans) {
                if (bean.supportTag(msg.getTags())) {
                    if (bean.excute(getRealMsg(s))) {
                        redisCacheUtils.setCacheObject(MQ_COMMIT_CACHE + msgId, true, 60 * 60 * 12);
                        log.info("本地事务执行成功，发送【确认】消息:{}", s);
                        return LocalTransactionState.COMMIT_MESSAGE;
                    }
                    break;
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
            String msgId = getMsgId(s);
            if (redisCacheUtils.exist(MQ_COMMIT_CACHE + msgId)) {
                log.info("mq重复回查事务消息:{}", s);
                return LocalTransactionState.COMMIT_MESSAGE;
            }
            log.info("mq回查事务消息:{}", s);
            for (ProducerDeal bean : beans) {
                if (bean.supportTag(msg.getTags())) {
                    if (bean.check(getRealMsg(s))) {
                        redisCacheUtils.setCacheObject(MQ_COMMIT_CACHE + msgId, true, 60 * 60 * 12);
                        log.info("mq回查事务消息，发送【确认】消息:{}", s);
                        return LocalTransactionState.COMMIT_MESSAGE;
                    }
                    break;
                }
            }
            log.info("mq回查事务消息，发送【回滚】消息:{}", s);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("mq回查事务消息，", e);
        }
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }

    private String getRealMsg(String msg) {
        if (msg == null || !msg.contains(SEPARATOR)) {
            return null;
        }
        return msg.substring(msg.indexOf(SEPARATOR) + 1);
    }

    private String getMsgId(String msg) {
        if (msg == null || !msg.contains(SEPARATOR)) {
            return null;
        }
        return msg.substring(0, msg.indexOf(SEPARATOR));
    }

}
