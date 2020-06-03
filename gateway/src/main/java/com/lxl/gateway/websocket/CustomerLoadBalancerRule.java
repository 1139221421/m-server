package com.lxl.gateway.websocket;

import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.Server;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义gateway负载均衡，websocket协议单独处理
 *
 * @author: lxl
 */
@Configuration
public class CustomerLoadBalancerRule extends AbstractLoadBalancerRule {

    @Override
    public Server choose(Object key) {
        List<Server> servers = this.getLoadBalancer().getReachableServers();
        if (servers.isEmpty()) {
            return null;
        }
        if (servers.size() == 1) {
            return servers.get(0);
        }
        if (key == null || "default".equals(key.toString())) {
            return randomChoose(servers);
        }
        return consistentHashChoose(servers, key);
    }

    /**
     * 随机返回一个服务实例
     *
     * @param servers
     */
    private Server randomChoose(List<Server> servers) {
        return servers.get(RandomUtils.nextInt(servers.size()));
    }

    /**
     * websocket采用一致性hash算法来确定与哪个服务建立连接
     *
     * @param servers
     * @param key
     */
    private Server consistentHashChoose(List<Server> servers, Object key) {
        ConsistentHash<Server> consistentHash = new ConsistentHash<>(200, servers);
        return consistentHash.get(key.toString());
    }

    @Override
    public void initWithNiwsConfig(IClientConfig config) {
    }

}
