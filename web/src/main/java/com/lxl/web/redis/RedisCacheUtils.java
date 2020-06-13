package com.lxl.web.redis;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * @description: redis缓存工具类
 */
@Component
public class RedisCacheUtils {
    private static Logger logger = LoggerFactory.getLogger(RedisCacheUtils.class);

    @Value("${spring.redis.serializerType:1}")
    private int serializerType;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate template) {
        this.redisTemplate = template;
    }

    public RedisCacheUtils(RedisTemplate redisTemplate) {
        this.setRedisTemplate(redisTemplate);
    }

    /**
     * 缓存任意对象
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     * @return
     */
    public boolean setCacheObject(String key, Object value) {
        return setCacheObject(key, value, 0);
    }

    /**
     * 缓存任意对象
     *
     * @param key
     * @param value
     * @param seconds 秒
     * @return
     */
    public boolean setCacheObject(String key, Object value, long seconds) {
        logger.debug("存入缓存 key:" + key);
        RedisTemplate template = getRedisSerializer(value.getClass());
        try {
            ValueOperations<String, Object> operation = template.opsForValue();
            if (seconds > 0) {
                operation.set(key, value, seconds, TimeUnit.SECONDS);
            } else {
                operation.set(key, value);
            }
            return true;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return false;
        }
    }

    /**
     * 当key不存在时，才能存放key
     *
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public boolean setIfAbsent(String key, Object value, long seconds) {
        logger.debug("存入缓存 key且key不存在:" + key);
        RedisTemplate template = getRedisSerializer(value.getClass());
        try {
            ValueOperations<String, Object> operation = template.opsForValue();
            if (seconds > 0) {
                return operation.setIfAbsent(key, value, seconds, TimeUnit.SECONDS);
            } else {
                return operation.setIfAbsent(key, value);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return false;
        }
    }

    public boolean setIfAbsent(String key, Object value) {
        return setIfAbsent(key, value, 0);
    }

    /**
     * 根据pattern匹配清除缓存
     *
     * @param pattern
     */
    public void clear(String pattern) {
        logger.debug("清除缓存 pattern:" + pattern);
        try {
            ValueOperations<String, Object> valueOper = redisTemplate.opsForValue();
            RedisOperations<String, Object> redisOperations = valueOper.getOperations();
            redisOperations.keys(pattern);
            Set<String> keys = redisOperations.keys(pattern);
            for (String key : keys) {
                redisOperations.delete(key);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return;
        }
    }

    /**
     * 根据key清除缓存
     *
     * @param key
     */
    public void delete(String key) {
        logger.debug("删除缓存 key:" + key);
        try {
            ValueOperations<String, Object> valueOper = redisTemplate.opsForValue();
            RedisOperations<String, Object> redisOperations = valueOper.getOperations();
            redisOperations.delete(key);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return;
        }
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public Object getCacheObject(String key) {
        logger.debug("获取缓存 key:" + key);
        try {
            ValueOperations<String, Object> operation = redisTemplate.opsForValue();
            return operation.get(key);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return null;
        }
    }

    /**
     * hash设值
     *
     * @param key
     * @param hashKey
     * @param val
     */
    public void hSet(String key, String hashKey, Object val) {
        hSet(key, hashKey, val, 0, null);
    }

    /**
     * hash设值
     *
     * @param key
     * @param hashKey
     * @param val
     */
    public void hSet(String key, String hashKey, Object val, long timeout) {
        hSet(key, hashKey, val, timeout, TimeUnit.SECONDS);
    }

    /**
     * hash设值
     *
     * @param key
     * @param hashKey
     * @param val
     */
    public void hSet(String key, String hashKey, Object val, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForHash().put(key, hashKey, val);
        if (timeout > 0) {
            redisTemplate.expire(key, timeout, timeUnit);
        }
    }

    /**
     * 增加值
     *
     * @param key
     * @param hashKey
     * @param val
     */
    public Long hIncrBy(String key, String hashKey, Long val) {
        return redisTemplate.opsForHash().increment(key, hashKey, val);
    }

    /**
     * 增加值
     *
     * @param key
     * @param hashKey
     * @param val
     */
    public Double hIncrBy(String key, String hashKey, double val) {
        return redisTemplate.opsForHash().increment(key, hashKey, val);
    }

    /**
     * 批量设值hash值
     *
     * @param key
     * @param data
     */
    public void hMSet(String key, Map data) {
        hMSet(key, data, 0);
    }

    /**
     * 批量设值hash值
     *
     * @param key
     * @param data
     */
    public void hMSet(String key, Map data, long timeout) {
        hMSet(key, data, timeout, TimeUnit.SECONDS);
    }

    /**
     * 批量设值hash值
     *
     * @param key
     * @param data
     */
    public void hMSet(String key, Map data, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForHash().putAll(key, data);
        if (timeout > 0) {
            redisTemplate.expire(key, timeout, timeUnit);
        }
    }

    /**
     * hash值是否存在
     *
     * @param key
     * @param hashKey
     * @return
     */
    public Boolean hExists(String key, Object hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * zset 增肌值
     *
     * @param key
     * @param val
     * @param delta
     * @return
     */
    public Double zIncrBy(String key, Object val, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, val, delta);
    }

    /**
     * zsort添加元素
     *
     * @param key
     * @param val
     * @param delta
     * @return
     */
    public Boolean zAdd(String key, Object val, Long delta) {
        return redisTemplate.opsForZSet().add(key, val, delta);
    }

    public Set zRevRange(String key, int start, int stop) {
        return redisTemplate.opsForZSet().reverseRange(key, start, stop);
    }

    /**
     * 加值
     *
     * @param key
     * @param typedTuples
     * @return
     */
    public Long zAdd(String key, Set typedTuples) {
        return redisTemplate.opsForZSet().add(key, typedTuples);
    }

    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return redisTemplate.expire(key, timeout, timeUnit);
    }

    /**
     * 判断是否存在该key
     *
     * @param key
     * @return
     */
    public Boolean exist(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 获取hash值全部
     *
     * @param key
     * @return
     */
    public Map hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public List hGetAll(String key, Collection hashKey) {
        return redisTemplate.opsForHash().multiGet(key, hashKey);
    }

    /**
     * 获取hash值
     *
     * @param key
     * @param field
     * @return
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 判断是否是set的一员
     *
     * @param key
     * @param member
     * @return
     */
    public Boolean sIsMember(String key, Object member) {
        return redisTemplate.opsForSet().isMember(key, member);
    }

    /**
     * 集合添加
     *
     * @param key
     * @param member
     * @return
     */
    public Long sAdd(String key, Object... member) {
        return redisTemplate.opsForSet().add(key, member);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(String key, Class<T> clazz) {
        logger.debug("获取缓存 key:" + key);
        RedisTemplate template = getRedisSerializer(clazz);
        try {
            ValueOperations<String, T> operation = template.opsForValue();
            Object object = operation.get(key);
            T result = null;
            if (object != null) {
                result = (T) object;
            }
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return null;
        }
    }

    private <T> RedisTemplate getRedisSerializer(Class<T> clazz) {
        RedisSerializer redisSerializer = null;
        switch (serializerType) {
            case 1:
                //使用FastJSON序列化
                redisSerializer = new FastJsonRedisSerializer(clazz);
                break;
            default:
                //使用Jackson序列化
                redisSerializer = new Jackson2JsonRedisSerializer(clazz);
                break;
        }
        RedisTemplate template = new StringRedisTemplate(redisConnectionFactory);
        template.setValueSerializer(redisSerializer);
        template.setHashValueSerializer(redisSerializer);
        return template;
    }
}
