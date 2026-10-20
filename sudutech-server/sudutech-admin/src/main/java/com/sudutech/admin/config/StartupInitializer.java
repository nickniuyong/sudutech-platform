package com.sudutech.admin.config;

import com.sudutech.system.service.SysJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动后初始化：恢复定时任务调度
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupInitializer implements CommandLineRunner {

    private final SysJobService jobService;

    @Override
    public void run(String... args) {
        try {
            jobService.initSchedule();
        } catch (Exception e) {
            log.warn("定时任务初始化失败（不影响系统启动）", e);
        }
    }
}
