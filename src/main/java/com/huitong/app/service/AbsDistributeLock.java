package com.huitong.app.service;

import com.huitong.app.exception.RTException;
import com.huitong.app.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.Collections;

/**
 * <p>
 * 单台 REDIS 中使用，非严格意义的分布式锁。 <br/>
 * 1） 执行时间超过设置的过期时间，会有多个获得锁 <br/>
 * 2） 一台获得锁的机器，执行了一次 FULL GC，超过有效时间，redis中的锁释放了，可能造成多个获得锁。 <br/>
 * 3） redis 获取锁失败，全部没有执行
 * 4） redis 宕机，后续全部没有执行
 * </p>
 * author pczhao<br/>
 * date  2019-07-04 10:56
 */

@Slf4j
public abstract class AbsDistributeLock {

    private Jedis jedis;

    public void initDistributeLock(String ip, int port, Integer database, String password) {
        jedis = new Jedis(ip, port);
        if (password != null && !password.isEmpty()) {
            jedis.auth(password.trim());
        }
        if (database == null || database < 0 || database > 15) {
            database = 0;
        }
        jedis.select(database);
    }

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    private static final Long RELEASE_SUCCESS = 1L;

    /**
     * 具体的任务需要子类去实现
     *
     * @throws RTException 分布式锁异常
     */
    public abstract void taskService() throws RTException;

    /**
     * 任一执行，ANY OF
     * 所有节点任意一个执行任务 taskService 即可，没有获得锁的节点不执行任务
     *
     * @param lockKey    lockKey
     * @param keyValue   keyValue
     * @param expireTime 过期时间 ms
     */
    public void onlyOneNodeExecute(String lockKey, String keyValue, int expireTime) {
        boolean getLock = false;
        try {
            if ((getLock = getDistributedLock(jedis, lockKey, keyValue, expireTime))) {
                taskService();
            }
        } finally {
            if (getLock) {
                releaseDistributedLock(jedis, lockKey, keyValue);
            }
        }
    }

    /**
     * 所有串行执行，ALL IN LINE
     * 所有节点都必须执行该任务，每次只能一个执行。
     *
     * @param lockKey    lockKey
     * @param keyValue   keyValue
     * @param expireTime 过期时间 ms
     */
    public void allNodeExecute(String lockKey, String keyValue, int expireTime) {
        try {
            while (!(getDistributedLock(jedis, lockKey, keyValue, expireTime))) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    log.info(e.getMessage());
                }
            }
            taskService();
        } finally {
            releaseDistributedLock(jedis, lockKey, keyValue);
        }
    }

    /**
     * @param jedis      客户端
     * @param lockKey    key
     * @param keyValue   key值
     * @param expireTime 过期时间，ms
     */
    public static boolean getDistributedLock(Jedis jedis, String lockKey, String keyValue, int expireTime) {
        String result = jedis.set(lockKey, keyValue, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        if (LOCK_SUCCESS.equals(result)) {
            log.info("ip:[{}] get lock:[{}], value:[{}], getLock result:[{}]", IpUtil.getLocalIpAddr(), lockKey, keyValue, result);
            return true;
        } else {
            return false;
        }
    }

    public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String keyValue) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(keyValue));
        log.info("ip:[{}] release lock:[{}], value:[{}], release result: [{}]", IpUtil.getLocalIpAddr(), lockKey, keyValue, result);
        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

}
