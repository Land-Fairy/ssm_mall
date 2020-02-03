package com.mmall.util;

import com.mmall.common.RedisSharedPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;

@Slf4j
public class RedisSharedPoolUtil {
    /**
     * 设置 key 的超时时间
     * @param key
     * @param exTime 单位 秒
     * @return 1 -> 成功
     */
    public static Long expire(String key, int exTime) {
        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisSharedPool.getJedis();
            result = jedis.expire(key, exTime);
        } catch (Exception e) {
            log.error("expire key: {}  error", key, e);
            RedisSharedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisSharedPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置 带有 超时时间的 key
     * @param key
     * @param value
     * @param exTime
     * @return
     */
    public static String setEx(String key, String value, int exTime) {
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisSharedPool.getJedis();
            result = jedis.setex(key, exTime, value);
        } catch (Exception e) {
            log.error("setex key: {} value: {} error", key, value, e);
            RedisSharedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisSharedPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置 Key value
     * @param key
     * @param value
     * @return
     */
    public static String set(String key, String value) {
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisSharedPool.getJedis();
            result = jedis.set(key,value);
        } catch (Exception e) {
            log.error("set key: {} value: {} error", key, value, e);
            RedisSharedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisSharedPool.returnResource(jedis);
        return result;
    }

    /**
     * 获取key的值
     * @param key
     * @return
     */
    public static String get(String key) {
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisSharedPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("get key: {} error", key, e);
            RedisSharedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisSharedPool.returnResource(jedis);
        return result;
    }

    /**
     * 删除 key
     * @param key
     * @return
     */
    public static Long del(String key) {
        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisSharedPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del key: {} error", key, e);
            RedisSharedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisSharedPool.returnResource(jedis);
        return result;
    }
}
