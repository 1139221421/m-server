package com.lxl.web.lock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LockConfig {
    @Bean(destroyMethod = "close")
    public DistLock distLock() {
        return new DistLock();
    }
}
