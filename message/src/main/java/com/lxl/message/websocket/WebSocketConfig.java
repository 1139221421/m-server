//package com.lxl.message.websocket;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
///**
// * websocket配置
// *
// * @author
// */
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//    /**
//     * 添加一个服务端点，来接收客户端的连接
//     *
//     * @param registry
//     */
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        // 添加了一个/websocket端点 设置跨域 开启socketjs支持，客户端就可以通过这个端点来进行连接
//        registry.addEndpoint("/websocket").setAllowedOrigins("*").withSockJS();
//    }
//
//    /**
//     * 定义消息代理,设置消息连接请求的各种规范信息
//     *
//     * @param registry
//     */
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        // 客户端订阅地址的前缀信息 topic广播  user一堆一
//        registry.enableSimpleBroker("/topic", "/user");
//        // 服务端接收地址的前缀
//        registry.setApplicationDestinationPrefixes("/app");
//    }
//}
