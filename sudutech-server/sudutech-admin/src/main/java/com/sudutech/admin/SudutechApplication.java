package com.sudutech.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * sudutech 快速开发平台 启动入口
 */
@EnableAsync
@EnableTransactionManagement
@MapperScan("com.sudutech.**.mapper")
@SpringBootApplication(scanBasePackages = "com.sudutech")
public class SudutechApplication {

    public static void main(String[] args) {
        SpringApplication.run(SudutechApplication.class, args);
        System.out.println("""

                ========================================================
                   数岛软件 · sudutech 快速开发平台 启动成功!
                   接口文档: http://localhost:8080/doc.html
                ========================================================
                """);
    }
}
