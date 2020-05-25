package com.lxl.eureka;

import com.lxl.utils.config.ConfUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaApplication {

    public static void main(String[] args) throws Exception {
        ConfUtil.initAndSetProperties();
        SpringApplication.run(EurekaApplication.class, args);
    }

}
