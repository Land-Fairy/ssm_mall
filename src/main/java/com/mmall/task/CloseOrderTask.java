package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisSharedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * TODO: 定时关闭订单
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;

    /* 版本一:
     * 方式一:
        缺点:  在多 tomcat时，多个 tomcat 都会 去读取数据库，进行 执行任务 ==> 同一个任务，被执行多次
     * 每 隔 一分钟
     */
//    @Scheduled(cron = "0 */1 * * * ? ")
//    public void closeOrderTaskV1() {
//        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
//        iOrderService.closeOrder(hour);
//    }

    /**
     * 版本二: 使用 Redis分布锁
     * 每 隔 一分钟
     * 使用 Redis 分布式锁 版本
     */
//    @Scheduled(cron = "0 */1 * * * ? ")
//    public void closeOrderTaskV2() {
//        /* 分布式锁 的超时时间 单位: 毫秒 */
//        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
//
//        /**
//         * 分布式锁
//         * value 为：当前时间 + 过期时间  ==》 记录了锁的时间，可以判断锁是否过期，防止死锁
//         */
//        Long setResult = RedisSharedPoolUtil.setnx(Const.RedisLock.CLOSE_ORDER_LOCK,
//                String.valueOf(System.currentTimeMillis() + lockTimeout));
//
//        if (setResult != null && setResult.intValue() == 1) {
//            /* 获取锁成功*/
//            closeOrder(Const.RedisLock.CLOSE_ORDER_LOCK);
//        } else {
//            log.info("没有获取到分布式锁");
//        }
//        log.info("关闭订单 定时任务结束");
//
//    }

    private void closeOrder(String lockName) {
        RedisSharedPoolUtil.expire(lockName, 5);
        log.info("获取{}, ThreadName: {}", Const.RedisLock.CLOSE_ORDER_LOCK, Thread.currentThread().getName());

        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);
        RedisSharedPoolUtil.del(Const.RedisLock.CLOSE_ORDER_LOCK);
        log.info("释放{}, ThreadName: {}", Const.RedisLock.CLOSE_ORDER_LOCK, Thread.currentThread().getName());
    }

    /**
     * 版本三: Redis 分布锁版本，添加防止 死锁功能
     * 每 隔 一分钟
     * 使用 Redis 分布式锁 版本
     */
//    @Scheduled(cron = "0 */1 * * * ? ")
    public void closeOrderTaskV3() {
        /* 分布式锁 的超时时间 单位: 毫秒 */
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));

        /**
         * 分布式锁
         * value 为：当前时间 + 过期时间  ==》 记录了锁的时间，可以判断锁是否过期，防止死锁
         */
        Long setResult = RedisSharedPoolUtil.setnx(Const.RedisLock.CLOSE_ORDER_LOCK,
                String.valueOf(System.currentTimeMillis() + lockTimeout));

        if (setResult != null && setResult.intValue() == 1) {
            /* 获取锁成功*/
            closeOrder(Const.RedisLock.CLOSE_ORDER_LOCK);
        } else {
            /**
             * 如果 setnx 之后，程序退出，那么 key 就没有设置超时，锁就一直不会过期，造成死锁的问题
             *  解决：在没有获取到锁的时候，判断下 锁的时间 是否已经过期
             */
            String lockValueStr = RedisSharedPoolUtil.get(Const.RedisLock.CLOSE_ORDER_LOCK);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                /**
                 * 由于是 多 tomcat 方式，可能 会有 多个 进程 同时判断 锁已经超时，都走到了这里
                 * 那么，哪个进程可以 获取成功呢？
                 * 由于 getSet 是 原子性的，返回的 是旧值，即使多个 进程可以同时执行，也会有一个先后 (getSet 返回值 == lockValueStr 值的，
                 * 就认为 是 获取 锁成功过的！！)
                 */
                String oldLockValueStr = RedisSharedPoolUtil.getSet(Const.RedisLock.CLOSE_ORDER_LOCK,
                        String.valueOf(System.currentTimeMillis() + lockTimeout));

                if (oldLockValueStr == null ||
                        (oldLockValueStr != null &&  StringUtils.equals(oldLockValueStr, lockValueStr))) {
                    /* 获取锁成功*/
                    closeOrder(Const.RedisLock.CLOSE_ORDER_LOCK);
                }
            } else {
                log.info("没有获取到分布式锁");
            }
        }
        log.info("关闭订单 定时任务结束");
    }


    /**
     * 版本四: Redisson 实现分布式锁版本
     */
    @Scheduled(cron = "0 */1 * * * ? ")
    public void closeOrderTaskV4() {
        RLock lock = redissonManager.getRedisson().getLock(Const.RedisLock.CLOSE_ORDER_LOCK);
        boolean getLock = false;
        try {
            /**TODO
             * waitTime: 等待获取锁的时间, 一般设置为 0
             * 原因:
             *  本来是想要 只有一个人能获取到锁（执行代码), 假定 waitTime=5
             *  1. 第一个 tomcat 获取到锁，执行代码
             *  2. 第二个 tomcat 等待 获取锁（等待 waitTime 时间)
             *  3. 第一个 tomcat 执行完毕，释放锁(不到 waitTime时间)
             *  4. 第二个 tomcat 也获取到锁，执行代码
             *  ====> 两个 tomcat 先后都获取到锁了 ！！！！！
             *  第二个 tomcat 没获取到所，就应该直接失败！！！
             * leaseTime: 释放锁等待时间
             */
            getLock = lock.tryLock(0, 5, TimeUnit.SECONDS);
            if (getLock) {
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
                iOrderService.closeOrder(hour);
            } else {
                log.info("么有获取到分布式锁");
            }

        } catch (InterruptedException e) {
            log.error("Redisson分布式锁获取异常", e);
        } finally {
            /* 如果没有获取到分布式锁，跳过*/
            if (!getLock) {
                return;
            }
            /* 释放锁 */
            lock.unlock();
        }

    }
}
