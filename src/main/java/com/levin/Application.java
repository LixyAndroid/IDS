package com.levin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


/**
 * SpringBoot程序入口
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class Application {

    @Bean
    public ThreadPoolTaskScheduler getTaskPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        return threadPoolTaskScheduler;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
