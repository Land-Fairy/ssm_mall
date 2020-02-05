package com.mmall.common;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

/**
 * TODO：GuavaCache
 * 本来是用来单机保存 token的，但是在使用了 多 tomcat 架构之后
 * 使用 Redis 进行了替代
 */
public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static String TOKEN_PREFIX = "token_";

    private static LoadingCache<String, String> localCache = CacheBuilder
            .newBuilder().initialCapacity(1000)
            .maximumSize(10000)
            .expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String key) throws Exception {
                    /* 使用 "null" 字符串 替换 null, 防止在
                    * 字符串比较的时候，出现异常 */
                    return "null";
                }});


    public static void setKey(String key, String value) {
        localCache.put(key, value);
    }

    public static String getKey(String key) {
        String value = null;
        try {
            value = localCache.get(key);

        } catch (Exception e) {
            logger.error("localCache get error: ", e);
        }
        return value;
    }
}
