package com.lxl.message.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yeauty.standard.ServerEndpointExporter;

/**
 * Author: chrisliu
 * Date: 2019/3/28 14:44
 * Mail: gwarmdll@gmail.com
 */
@Configuration
public class WebSocketConfig1 {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
