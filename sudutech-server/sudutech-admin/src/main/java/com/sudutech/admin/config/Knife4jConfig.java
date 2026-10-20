package com.sudutech.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 3 文档
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("sudutech 快速开发平台 API")
                .description("系统管理 / 工作流 / 大屏 / 报表 / AI问数 / 代码生成")
                .version("v1.0"));
    }
}
