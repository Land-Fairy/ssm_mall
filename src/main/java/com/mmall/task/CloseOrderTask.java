package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisSharedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

//    /**
//     * 每 隔 一分钟
//     */
//    @Scheduled(cron = "0 */1 * * * ? ")
//    public void closeOrderTaskV1() {
//        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
//        iOrderService.closeOrder(hour);
//    }

    /**
     * 每 隔 一分钟
     * 使用 Redis 分布式锁 版本
     */
    @Scheduled(cron = "0 */1 * * * ? ")
    public void closeOrderTaskV2() {
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
            log.info("没有获取到分布式锁");
        }
        log.info("关闭订单 定时任务结束");

    }

    private void closeOrder(String lockName) {
        RedisSharedPoolUtil.expire(lockName, 5);
        log.info("获取{}, ThreadName: {}", Const.RedisLock.CLOSE_ORDER_LOCK, Thread.currentThread().getName());

        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);
        RedisSharedPoolUtil.del(Const.RedisLock.CLOSE_ORDER_LOCK);
        log.info("释放{}, ThreadName: {}", Const.RedisLock.CLOSE_ORDER_LOCK, Thread.currentThread().getName());
    }
}
