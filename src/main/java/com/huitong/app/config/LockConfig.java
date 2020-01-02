package com.huitong.app.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * author pczhao
 * date  2019-12-31 16:56
 */

@Getter
@Configuration
public class LockConfig {

    @Value("${app.redis.expireTime}")
    private int redisExpireTime;

    @Value("${app.redis.ip}")
    private String redisIp;

    @Value("${app.redis.port}")
    private int redisPort;

    @Value("${app.redis.password}")
    private String password;

    @Value("${app.redis.database}")
    private int database;

}
