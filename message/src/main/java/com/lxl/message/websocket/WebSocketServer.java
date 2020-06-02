//package com.lxl.message.websocket;
//
//import com.alibaba.fastjson.JSONObject;
//import com.lxl.common.enums.MqTagsEnum;
//import com.lxl.web.mq.ConsumerDeal;
//import com.lxl.web.mq.RocketMqConsumer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//import javax.annotation.Resource;
//
///**
// * websocket消息发送
// */
//@Controller
//public class WebSocketServer implements ConsumerDeal {
//    private static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
//
//    @Resource
//    private SimpMessagingTemplate simpMessagingTemplate;
//
//    @Resource
//    private RocketMqConsumer rocketMqConsumer;
//
//    /**
//     * 广播通知
//     *
//     * @param jsonObject
//     */
//    @MessageMapping("/sendTo")
//    public void sendTo(JSONObject jsonObject) {
//        rocketMqConsumer.sendBroadcastMsg(jsonObject.toJSONString(), MqTagsEnum.WEBSOCKET_MSG);
//    }
//
//    /**
//     * 发送给用户
//     *
//     * @param jsonObject
//     */
//    @MessageMapping("/sendToUser")
//    public void sendToUser(JSONObject jsonObject) {
//        rocketMqConsumer.sendBroadcastMsg(jsonObject.toJSONString(), MqTagsEnum.WEBSOCKET_MSG);
//    }
//
//    @Override
//    public boolean supportTag(String tagsEnum) {
//        return MqTagsEnum.WEBSOCKET_MSG.getTagName().equals(tagsEnum);
//    }
//
//    @Override
//    public boolean deal(String msg) {
//        String desc = MqTagsEnum.WEBSOCKET_MSG.getTagDesc();
//        logger.info("{} 取得消息：{}", desc, msg);
//        JSONObject jsonObject = JSONObject.parseObject(msg);
//        if (!jsonObject.containsKey("msg")) {
//            return true;
//        }
//        if (jsonObject.containsKey("user")) {
//            // 一对一
//            this.simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("user"), "/sendToUser", jsonObject.getString("msg"));
//        } else {
//            // 群发
//            this.simpMessagingTemplate.convertAndSend("/topic/notice", jsonObject.getString("msg"));
//        }
//        return true;
//    }
//
//}
