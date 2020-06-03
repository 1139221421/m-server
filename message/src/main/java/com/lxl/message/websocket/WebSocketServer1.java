package com.lxl.message.websocket;

import com.alibaba.fastjson.JSON;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.yeauty.annotation.*;
import org.yeauty.pojo.Session;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 配置详解 https://github.com/YeautyYE/netty-websocket-spring-boot-starter/blob/master/README_zh.md
 * Author: lxl
 */
@ServerEndpoint(path = "/message/{arg}", port = "${netty-websocket.discovery.client.port}")
@Component
public class WebSocketServer1 {

    private final static Logger logger = LoggerFactory.getLogger(WebSocketServer1.class);

    private static CopyOnWriteArraySet<WsSession> sessionSet = new CopyOnWriteArraySet<WsSession>();

    /**
     * 当有新的连接进入时，对该方法进行回调
     *
     * @param session
     * @param headers
     * @param req
     * @param reqMap
     * @param arg
     * @param pathMap
     */
    @BeforeHandshake
    public void handshake(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap) {
        session.setSubprotocols("stomp");
        logger.info("websocket:one connection entering, reqMap:{},pathMap:{}", JSON.toJSONString(reqMap), JSON.toJSONString(pathMap));
//        if (!req.equals("ok")) {
//            session.close();
//        }
    }

    /**
     * 当有新的WebSocket连接完成时，对该方法进行回调
     *
     * @param session
     * @param headers
     * @param req
     * @param reqMap
     * @param arg
     * @param pathMap
     */
    @OnOpen
    public void onOpen(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap) {
        sessionSet.add(new WsSession(session, Long.valueOf(arg)));
        logger.info("websocket:one connection established, reqMap:{},pathMap:{}", JSON.toJSONString(reqMap), JSON.toJSONString(pathMap));
    }

    /**
     * 连接关闭时
     *
     * @param session
     * @throws IOException
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        sessionSet.remove(new WsSession(session));
        logger.info("websocket:one connection closed...");
    }

    /**
     * 抛出异常
     *
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("websocket:one connection throw exception ", throwable);
    }

    /**
     * 接收到字符串消息
     *
     * @param session
     * @param message
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        logger.info("websocket:received string message:{} ", message);
        session.sendText("Hello Netty!");
    }

    /**
     * 接收到二进制消息
     *
     * @param session
     * @param bytes
     */
    @OnBinary
    public void onBinary(Session session, byte[] bytes) {
        logger.info("websocket:received byte message:{} ", new String(bytes));
    }

    /**
     * 接收到Netty的事件
     *
     * @param session
     * @param evt
     */
    @OnEvent
    public void onEvent(Session session, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    logger.debug("websocket:read idle");
                    break;
                case WRITER_IDLE:
                    logger.debug("websocket:write idle");
                    break;
                case ALL_IDLE:
                    logger.debug("websocket:all idle");
                    break;
                default:
                    break;
            }
        }
    }
}
