package com.lxl.auth;

import com.lxl.utils.config.ConfUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableHystrix
@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.lxl.common.feign"})
@MapperScan(basePackages = "com.lxl.*.dao")
// seata必须禁用Springboot的dataSources自动装配
@SpringBootApplication(scanBasePackages = "com.lxl", exclude = DataSourceAutoConfiguration.class)
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

        // 使用分布式事务seata
        // 1.服务器启动seata-server
        // 2.客户禁用@SpringBootApplication(exclude = DataSourceAutoConfiguration.class) dataSources自动装配
        // 3.配置seata数据源搭理 DataSourceConfig
        // 4.file.conf和registry.conf, 注意file.conf中disableGlobalTransaction=false才开启seata
        System.setProperty("spring.cloud.alibaba.seata.tx-service-group", "my_test_tx_group");

        //Hystrix全局超时时间配置,使用@HystrixCommand注解实现（可以不配置注解参数）
        System.setProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds", "6000");
        //Hystrix针对某个方法超时时间配置,使用@HystrixCommand(commandKey = "test")注解实现
        System.setProperty("hystrix.command.test.execution.isolation.thread.timeoutInMilliseconds", "1000");

        System.setProperty("spring.elasticsearch.rest.uris[0]", "http://dev:9201");
        System.setProperty("spring.elasticsearch.rest.uris[1]", "http://dev:9202");
        System.setProperty("spring.elasticsearch.rest.uris[2]", "http://dev:9203");
        System.setProperty("spring.elasticsearch.rest.username", "elastic");
        System.setProperty("spring.elasticsearch.rest.password", "123456");
        SpringApplication.run(AuthApplication.class, args);
    }

}
