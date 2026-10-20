package com.sudutech.system.quartz;

import com.sudutech.system.domain.SysJob;
import com.sudutech.system.domain.SysJobLog;
import com.sudutech.system.mapper.SysJobLogMapper;
import com.sudutech.system.util.JobInvokeUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Quartz 执行入口：同一任务禁止并发
 */
@Slf4j
@Component
@DisallowConcurrentExecution
public class QuartzJobExecution implements Job {

    @Autowired
    private JobInvokeUtil jobInvokeUtil;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void execute(JobExecutionContext context) {
        SysJob job = (SysJob) context.getMergedJobDataMap()
                .get(com.sudutech.system.quartz.ScheduleConstants.JOB_DATA_KEY);

        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobName(job.getJobName());
        jobLog.setJobGroup(job.getJobGroup());
        jobLog.setInvokeTarget(job.getInvokeTarget());
        jobLog.setJobTime(LocalDateTime.now());

        long start = System.currentTimeMillis();
        try {
            jobInvokeUtil.invoke(job.getInvokeTarget());
            long cost = System.currentTimeMillis() - start;
            jobLog.setJobMessage(job.getJobName() + " 执行成功，耗时 " + cost + "ms");
            jobLog.setStatus("0");
        } catch (Exception e) {
            log.error("定时任务执行失败: {}", job.getInvokeTarget(), e);
            long cost = System.currentTimeMillis() - start;
            jobLog.setJobMessage(job.getJobName() + " 执行失败，耗时 " + cost + "ms");
            jobLog.setStatus("1");
            String msg = String.valueOf(e.getMessage());
            jobLog.setExceptionInfo(msg.length() > 2000 ? msg.substring(0, 2000) : msg);
        } finally {
            try {
                applicationContext.getBean(SysJobLogMapper.class).insert(jobLog);
            } catch (Exception ex) {
                log.warn("保存任务日志失败", ex);
            }
        }
    }
}
