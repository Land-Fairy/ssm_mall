package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import sun.jvm.hotspot.debugger.win32.coff.COFFFile;

public class RedisPool {
    /* jedis 连接池 */
    private static JedisPool pool;

    /* 最大连接数 */
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));

    /* 做多空闲 jedis 实例个数 */
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));

    /* 最小的空闲 jedis 实例个数 */
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));

    /**
     * 在 borrow 一个 jedis实例时候，
     * 是否要进行验证操作(true 表示 每次取出来的肯定是可用实例)
     */
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));

    /**
     * 在 return 一个实例的时候，进行验证
     * 如果为 true，表示 放入的一个实例肯定是可用的
     */
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));

    private static String redisIp = PropertiesUtil.getProperty("redis.ip");
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));


    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        /**
         * 连接耗尽的时候，有新的连接来临
         *  true: 阻塞 直到超时 或者可用
         *  fale：抛出异常
         */
        config.setBlockWhenExhausted(true);

        pool = new JedisPool(config, redisIp, redisPort, 1000 * 2);
    }

    static {
        initPool();
    }

    /**
     * 获取一个 Jedis 连接
     * @return
     */
    public static Jedis getJedis() {
        return pool.getResource();
    }

    public static void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }

    public static void returnBrokenResource(Jedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis = RedisPool.getJedis();
        jedis.set("sssss", "aaaa");
        RedisPool.returnResource(jedis);
    }
}
