package com.lxl.web.config;

import cn.hutool.core.thread.NamedThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author
 */
@Configuration
public class ExecutorConfig {

    @Bean
    public ScheduledThreadPoolExecutor scheduledThreadPoolExecutor() {
        return new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1,
                new NamedThreadFactory("scheduled-", true));
    }
}
