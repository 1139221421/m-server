package com.lxl.auth;

import com.lxl.utils.config.ConfUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableEurekaClient
@SpringBootApplication(scanBasePackages = "com.lxl")
@EnableFeignClients(basePackages = {"com.lxl.common.feign"})
public class AuthApplication {

    public static void main(String[] args) throws Exception {
        ConfUtil.initAndSetProperties();
        SpringApplication.run(AuthApplication.class, args);
    }

}
