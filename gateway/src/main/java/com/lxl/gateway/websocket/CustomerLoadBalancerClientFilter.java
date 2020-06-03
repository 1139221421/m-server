package com.lxl.gateway.websocket;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

import java.net.URI;

import com.lxl.utils.config.ConfUtil;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.LoadBalancerClientFilter;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.web.server.ServerWebExchange;

/**
 * 实现LoadBalancerClientFilter，重写choose方法，过滤出websocket请求
 */
public class CustomerLoadBalancerClientFilter extends LoadBalancerClientFilter {

    public CustomerLoadBalancerClientFilter(LoadBalancerClient loadBalancer, LoadBalancerProperties properties) {
        super(loadBalancer, properties);
    }

    @Override
    protected ServiceInstance choose(ServerWebExchange exchange) {
        URI uri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        String serviceId = uri.getHost();
        if (this.loadBalancer instanceof RibbonLoadBalancerClient && serviceId.equals(ConfUtil.getPropertyOrDefault("ws-server", "WS"))) {
            return ((RibbonLoadBalancerClient) this.loadBalancer).choose(serviceId, uri.getPath());
        }
        return super.choose(exchange);
    }

}
