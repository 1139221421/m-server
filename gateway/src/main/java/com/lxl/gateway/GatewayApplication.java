package com.lxl.gateway;

import com.lxl.utils.config.ConfUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

@SpringCloudApplication
public class GatewayApplication {

    public static void main(String[] args) throws Exception {
        ConfUtil.initAndSetProperties();
        // 配置可跨域的
        System.setProperty("spring.cloud.gateway.globalcors.corsConfigurations.'[/**]'.allowedOrigins", "*");
        System.setProperty("spring.cloud.gateway.globalcors.corsConfigurations.'[/**]'.allowCredentials", "true");
        System.setProperty("spring.cloud.gateway.globalcors.corsConfigurations.'[/**]'.allowedMethods", "GET,PUT,POST,OPTIONS");
        System.setProperty("spring.cloud.gateway.globalcors.corsConfigurations.'[/**]'.allowedHeaders", "*");
        SpringApplication.run(GatewayApplication.class, args);
    }

}
