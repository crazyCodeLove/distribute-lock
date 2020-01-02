package com.huitong.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * <p></p>
 * author pczhao<br/>
 * date  2020-01-02 10:28
 */

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.huitong.app"})
@MapperScan("com.huitong.app")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
