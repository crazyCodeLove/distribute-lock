package com.huitong.app.service;

import com.huitong.app.exception.RTException;
import com.huitong.app.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * <p></p>
 * author pczhao<br/>
 * date  2020-01-03 11:03
 */

@Slf4j
public abstract class AbsDistributeLockLua {

    private RedisTemplate<String, String> redisTemplate;

    public AbsDistributeLockLua(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

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
            if ((getLock = getDistributeLock(redisTemplate, lockKey, keyValue, expireTime))) {
                taskService();
            }
        } finally {
            if (getLock) {
                releaseDistributeLock(redisTemplate, lockKey, keyValue);
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
            while (!(getDistributeLock(redisTemplate, lockKey, keyValue, expireTime))) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    log.info(e.getMessage());
                }
            }
            taskService();
        } finally {
            releaseDistributeLock(redisTemplate, lockKey, keyValue);
        }
    }

    /**
     * 通过lua脚本 加锁并设置过期时间
     *
     * @param key    锁key值
     * @param value  锁value值
     * @param expire 过期时间，单位毫秒
     * @return true：加锁成功，false：加锁失败
     */
    public boolean getDistributeLock(RedisTemplate<String, String> redisTemplate, String key, String value, int expire) {
        String strScript = "if redis.call('setNx',KEYS[1],ARGV[1])==1 then return redis.call('pexpire',KEYS[1],ARGV[2]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptText(strScript);
        try {
            Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value, expire);
            log.info("ip:[{}] get lock:[{}], value:[{}], getLock result:[{}]", IpUtil.getLocalIpAddr(), key, value, result);
            return result != null && result == 1;
        } catch (Exception e) {
            //可以自己做异常处理
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 通过lua脚本释放锁
     *
     * @param key   锁key值
     * @param value 锁value值（仅当redis里面的value值和传入的相同时才释放，避免释放其他线程的锁）
     * @return true：释放锁成功，false：释放锁失败（可能已过期或者已被释放）
     */
    public boolean releaseDistributeLock(RedisTemplate<String, String> redisTemplate, String key, String value) {
        String strScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptText(strScript);
        try {
            Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value);
            log.info("ip:[{}] release lock:[{}], value:[{}], release result: [{}]", IpUtil.getLocalIpAddr(), key, value, result);
            return result != null && result == 1;
        } catch (Exception e) {
            //可以自己做异常处理
            log.error(e.getMessage(), e);
            return false;
        }
    }

}
