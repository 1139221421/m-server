package com.lxl.eureka.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaRegistryAvailableEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaServerStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * eureka监听各服务状态
 *
 * @Author lxl
 */
@Component
public class EurekaStateChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EurekaStateChangeListener.class);

    /**
     * 服务下线
     *
     * @param eurekaInstanceCanceledEvent
     */
    @EventListener
    public void listen(EurekaInstanceCanceledEvent eurekaInstanceCanceledEvent) {
        LOGGER.info("{} 服务下线...", eurekaInstanceCanceledEvent.getAppName());
    }

    /**
     * 服务注册
     *
     * @param event
     */
    @EventListener
    public void listen(EurekaInstanceRegisteredEvent event) {
        LOGGER.info("{} 服务注册...", event.getInstanceInfo().getAppName());
    }

    /**
     * 服务续约
     *
     * @param event
     */
    @EventListener
    public void listen(EurekaInstanceRenewedEvent event) {
    }

    /**
     * eureka 注册注册中心启动
     *
     * @param event
     */
    @EventListener
    public void listen(EurekaRegistryAvailableEvent event) {
    }

    /**
     * eureka 服务启动
     *
     * @param event
     */
    @EventListener
    public void listen(EurekaServerStartedEvent event) {
    }
}
