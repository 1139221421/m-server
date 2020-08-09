package com.lxl.message;

import com.lxl.utils.config.ConfUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.lxl.common.feign"})
@MapperScan(basePackages = "com.lxl.*.dao")
// seata必须禁用Springboot的dataSources自动装配
@SpringBootApplication(scanBasePackages = "com.lxl", exclude = DataSourceAutoConfiguration.class)
public class MessageApplication {

    public static void main(String[] args) throws Exception {
        ConfUtil.initAndSetProperties();

        // 一下配置可以设置到zk，此处方便测试
//        System.setProperty("netty-websocket.host", "0.0.0.0");
//        System.setProperty("netty-websocket.path", "/message");
//        System.setProperty("netty-websocket.port", "8787");
        System.setProperty("netty-websocket.discovery.client.name", "ws");
        System.setProperty("netty-websocket.discovery.client.host", "127.0.0.1");
        System.setProperty("netty-websocket.discovery.client.port", "8787");
        System.setProperty("server.port", "6667");

        System.setProperty("spring.cloud.alibaba.seata.tx-service-group", "my_test_tx_group");
        SpringApplication.run(MessageApplication.class, args);
    }

}
