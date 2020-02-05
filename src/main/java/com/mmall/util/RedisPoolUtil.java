package com.mmall.util;

import com.mmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * TODO: RedisPool 工具类 (单 Redis 方式)
 */
@Slf4j
public class RedisPoolUtil {

    /**
     * 设置 key 的超时时间
     * @param key
     * @param exTime 单位 秒
     * @return 1 -> 成功
     */
    public static Long expire(String key, int exTime) {
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.expire(key, exTime);
        } catch (Exception e) {
            log.error("expire key: {}  error", key, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
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
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.setex(key, exTime, value);
        } catch (Exception e) {
            log.error("setex key: {} value: {} error", key, value, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置 Key value
     * @param key
     * @param value
     * @return
     */
    public static String set(String key, String value) {
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.set(key,value);
        } catch (Exception e) {
            log.error("set key: {} value: {} error", key, value, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 获取key的值
     * @param key
     * @return
     */
    public static String get(String key) {
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("get key: {} error", key, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 删除 key
     * @param key
     * @return
     */
    public static Long del(String key) {
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del key: {} error", key, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }


    public static void main(String[] args) {
        RedisPoolUtil.set("k1", "v1");
        String k1 = RedisSharedPoolUtil.get("k1");
        RedisSharedPoolUtil.setEx("k2", "v2", 100);
        RedisSharedPoolUtil.expire("k2", 2000);
        RedisSharedPoolUtil.del("k1");
    }
}

