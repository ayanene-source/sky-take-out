package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableCaching//缓存
@EnableScheduling//定时任务
public class SkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
        System.out.println("测试中文输出123");
        log.info("测试中文日志输出123");
        System.out.println("defaultCharset = " + java.nio.charset.Charset.defaultCharset());
System.out.println("file.encoding = " + System.getProperty("file.encoding"));
System.out.println("native.encoding = " + System.getProperty("native.encoding"));
System.out.println("sun.stdout.encoding = " + System.getProperty("sun.stdout.encoding"));
System.out.println("sun.stderr.encoding = " + System.getProperty("sun.stderr.encoding"));
    }
}
