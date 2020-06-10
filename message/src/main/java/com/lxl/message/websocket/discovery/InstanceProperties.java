package com.lxl.message.websocket.discovery;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(InstanceProperties.PREFIX)
public class InstanceProperties {
    public static final String PREFIX = "netty-websocket.discovery.client";

    String host;
    Integer port;
    String name;
}
