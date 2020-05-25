package com.lxl.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 权限验证过滤器
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    /**
     * 校验token是否有效
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpMethod method = request.getMethod();
        RequestPath path = request.getPath();
        String token = request.getHeaders().getFirst("Authroization");
        MultiValueMap<String, String> params = request.getQueryParams();
        logger.info("method:{},path:{},headers:{},params:{}", method, path, token, params);
        // 权限验证
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 值越小越先执行
        return 1;
    }
}
