package com.lxl.storage;

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
@SpringBootApplication(scanBasePackages = "com.lxl", exclude = DataSourceAutoConfiguration.class)
public class StorageApplication {

    public static void main(String[] args) throws Exception {
        ConfUtil.initAndSetProperties();

        System.setProperty("spring.cloud.alibaba.seata.tx-service-group", "my_test_tx_group");

        System.setProperty("spring.elasticsearch.rest.uris[0]", "http://dev:9201");
        System.setProperty("spring.elasticsearch.rest.uris[1]", "http://dev:9202");
        System.setProperty("spring.elasticsearch.rest.uris[2]", "http://dev:9203");
        System.setProperty("spring.elasticsearch.rest.username", "elastic");
        System.setProperty("spring.elasticsearch.rest.password", "123456");
        SpringApplication.run(StorageApplication.class, args);
    }

}
