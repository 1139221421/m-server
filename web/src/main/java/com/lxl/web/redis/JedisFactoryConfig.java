package com.lxl.web.redis;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

/**
 * redis配置
 */
@Configuration
@EnableCaching
@ConditionalOnBean(RedisConnectConfig.class)
public class JedisFactoryConfig extends CachingConfigurerSupport {
    @Value("${spring.redis.serializerType:1}")
    private int serializerType;
    //默认过期时间（秒）
    @Value("${spring.redis.expirationSecond:0}")
    private int expirationSecondTime;

    /**
     * 设置@cacheable 序列化方式
     *
     * @return
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
        configuration = configuration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(getRedisSerializer()));
        if (expirationSecondTime > 0) {
            // 默认过期时间
            configuration = configuration.entryTtl(Duration.ofSeconds(expirationSecondTime));
        }
        return configuration;
    }

    @Bean("jedisFactory")
    public RedisConnectionFactory jedisConnectionFactory(@Autowired RedisConnectConfig redisConnectConfig) {
        return new JedisConnectionFactory(redisConnectConfig);
    }

    @Bean
    public RedisTemplate redisTemplate(@Qualifier("jedisFactory") RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        RedisSerializer redisSerializer = null;
        switch (serializerType) {
            case 1:
                //使用FastJSON序列化
                redisSerializer = new FastJsonRedisSerializer(Object.class);
                break;
            default:
                //使用Jackson序列化
                redisSerializer = new Jackson2JsonRedisSerializer(Object.class);
                break;
        }
        template.setValueSerializer(redisSerializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(redisSerializer);
        return template;
    }

    private RedisSerializer getRedisSerializer() {
        RedisSerializer redisSerializer = null;
        switch (serializerType) {
            case 1:
                //使用FastJSON序列化
                redisSerializer = new GenericFastJsonRedisSerializer();
                break;
            default:
                //使用Jackson序列化
                redisSerializer = new GenericJackson2JsonRedisSerializer();
                break;
        }
        return redisSerializer;
    }
}
