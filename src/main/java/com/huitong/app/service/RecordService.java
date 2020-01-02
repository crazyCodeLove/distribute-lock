package com.huitong.app.service;

import com.huitong.app.config.LockConfig;
import com.huitong.app.dao.mapper.LockInfoMapper;
import com.huitong.app.exception.RTException;
import com.huitong.app.model.DisLockInfo;
import com.huitong.app.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 分布式锁使用简单例子
 * author pczhao
 * date  2019-12-31 16:55
 */

@Slf4j
@Service
public class RecordService extends AbstractDistributeLock {

    private LockConfig lockConfig;

    private static final String RECORD_UPDATE_LOCK_KEY = "sse:spcg:distribute:lock:record";

    @Autowired
    private LockInfoMapper lockInfoMapper;

    private Random random = new Random();
    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Autowired
    public RecordService(LockConfig lockConfig) {
        this.lockConfig = lockConfig;
        super.initDistributeLock(lockConfig.getRedisIp(), lockConfig.getRedisPort(), lockConfig.getDatabase(), lockConfig.getPassword());
    }

    @Transactional
    @Override
    public void taskService() throws RTException {
        long count = lockInfoMapper.queryRecordCount();
        count++;
        DisLockInfo lockInfo = DisLockInfo.builder()
                .ip(IpUtil.getLocalIpAddr())
                .number(count).build();
        lockInfoMapper.addLockInfo(lockInfo);
        try {
            Thread.sleep(30000 + random.nextInt(5000));
        } catch (InterruptedException e) {
            log.info("thread InterruptedException", e.getMessage());
        }
    }

    /**
     * 使用异步防止执行阻塞
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void schedulrTask() {
        executorService.submit(() -> {
            super.onlyOneExecute(RECORD_UPDATE_LOCK_KEY, UUID.randomUUID().toString(), lockConfig.getRedisExpireTime());
        });
    }


}
