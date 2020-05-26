package com.lxl.gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 网关编码配置化 与配置文件application.yml中配置的效果一样
 **/
@Configuration
public class GatewayConfig {
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

    /**
     * 限流配置
     *
     * @return
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(1, 1);
    }

    @Bean
    @ConditionalOnBean({RateLimiter.class, KeyResolver.class})
    public RequestRateLimiterGatewayFilterFactory customerRequestRateLimiterGatewayFilterFactory(RateLimiter rateLimiter, @Qualifier("keyResolver") KeyResolver resolver) {
        return new RequestRateLimiterGatewayFilterFactory(rateLimiter, resolver);
    }

    @Bean
    public RouteLocator customerRouteLocator(RouteLocatorBuilder builder,
                                             RequestRateLimiterGatewayFilterFactory customerRequestRateLimiterGatewayFilterFactory) {
        GatewayFilter filter = customerRequestRateLimiterGatewayFilterFactory.apply(new RequestRateLimiterGatewayFilterFactory.Config());
        return builder.routes()
                .route(r -> r.path("/auth/**").uri("lb://auth").filters(filter))
                .route(r -> r.path("/message/**").uri("lb://message"))
                .build();
    }

}
