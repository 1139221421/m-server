//package com.lxl.gateway.websocket;
//
//import java.util.List;
//
//import com.netflix.loadbalancer.ClientConfigEnabledRoundRobinRule;
//import com.netflix.loadbalancer.Server;
//import org.springframework.context.annotation.Configuration;
//
///**
// * 自定义gateway负载均衡，websocket协议单独处理
// * 本来是实现 AbstractLoadBalancerRule，普通请求无法使用默认的路由方式 super.choose(key)
// *
// * @author: lxl
// */
//@Configuration
//public class CustomerLoadBalancerRule extends ClientConfigEnabledRoundRobinRule {
//
//    @Override
//    public Server choose(Object key) {
//        if (key != null && !"default".equals(key.toString())) {
//            List<Server> servers = this.getLoadBalancer().getReachableServers();
//            return consistentHashChoose(servers, key);
//        } else {
//            // 使用默认路由规则
//            return super.choose(key);
//        }
//    }
//
//    /**
//     * websocket采用一致性hash算法来确定与哪个服务建立连接
//     *
//     * @param servers
//     * @param key
//     */
//    private Server consistentHashChoose(List<Server> servers, Object key) {
//        ConsistentHash<Server> consistentHash = new ConsistentHash<>(200, servers);
//        return consistentHash.get(key.toString());
//    }
//
//}
