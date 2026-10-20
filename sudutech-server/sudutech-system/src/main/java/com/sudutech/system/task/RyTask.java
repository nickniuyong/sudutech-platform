package com.sudutech.system.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 演示定时任务 Bean（供 sys_job.invoke_target 调用，如 ryTask.noParams()）
 */
@Slf4j
@Component("ryTask")
public class RyTask {

    public void noParams() {
        log.info("执行无参定时任务：{}", java.time.LocalDateTime.now());
    }

    public void withParams(String message, Integer count) {
        log.info("执行带参定时任务：message={}, count={}", message, count);
    }
}
