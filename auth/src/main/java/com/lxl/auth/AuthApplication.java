package com.lxl.auth;

import com.lxl.utils.config.ConfUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableHystrix
@EnableEurekaClient
@SpringBootApplication(scanBasePackages = "com.lxl")
@EnableFeignClients(basePackages = {"com.lxl.common.feign"})
@MapperScan(basePackages = "com.lxl.*.dao")
public class AuthApplication {

    public static void main(String[] args) throws Exception {
        ConfUtil.initAndSetProperties();

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

        //Hystrix全局超时时间配置,使用@HystrixCommand注解实现（可以不配置注解参数）
        System.setProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds", "6000");
        //Hystrix针对某个方法超时时间配置,使用@HystrixCommand(commandKey = "test")注解实现
        System.setProperty("hystrix.command.test.execution.isolation.thread.timeoutInMilliseconds", "1000");

        SpringApplication.run(AuthApplication.class, args);
    }

}
