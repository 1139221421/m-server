package com.lxl.web.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.stereotype.Component;

/**
 * redis连接配置
 */
@Component("redisConnectConfig")
@ConfigurationProperties(prefix = "spring.redis")
class RedisConnectConfig extends RedisStandaloneConfiguration {
}
