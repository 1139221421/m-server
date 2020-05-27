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
        // 以下流控配置应当写到disconf上，这里只是为了方便测试
        // 文件规则数据源
        System.setProperty("spring.cloud.sentinel.datasource.ds1.file.file", "classpath: flowRules.json");
        // JSON格式的数据
        System.setProperty("spring.cloud.sentinel.datasource.ds1.file.data-type", "json");
        // 规则类型
        System.setProperty("spring.cloud.sentinel.datasource.ds1.file.rule-type", "flow");
        // 限流控制台的地址和端口
        System.setProperty("spring.cloud.sentinel.transport.dashboard", "localhost:8086");
        // 取消Sentinel控制台懒加载
        System.setProperty("spring.cloud.sentinel.eager", "true");

        ConfUtil.initAndSetProperties();
        SpringApplication.run(AuthApplication.class, args);
    }

}
