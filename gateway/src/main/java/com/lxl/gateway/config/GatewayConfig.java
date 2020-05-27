package com.lxl.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 网关路由配置 与配置文件application.yml中配置的效果一样
 **/
@Configuration
public class GatewayConfig {
//    /**
//     * ip限流
//     *
//     * @return
//     */
//    @Bean
//    public KeyResolver keyResolver() {
//        // ip限流
//        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
////        // 用户限流
////        return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst("token"));
////        // 接口地址限流
////        return exchange -> Mono.just(exchange.getRequest().getPath().value());
//    }

    @Bean
    public RouteLocator customerRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/auth/**").uri("lb://auth"))
                .route(r -> r.path("/message/**").uri("lb://message"))
                // websocket集成
                .route(r -> r.path("/websocket/**").uri("lb:ws://message"))
                .build();
    }

}
