package com.lxl.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.reactive.HiddenHttpMethodFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class CustomResolver {
    /**
     * ip限流
     *
     * @return
     */
    @Bean
    public KeyResolver keyResolver() {
        // ip限流
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
//        // 用户限流
//        return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst("token"));
//        // 接口地址限流
//        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }

}
