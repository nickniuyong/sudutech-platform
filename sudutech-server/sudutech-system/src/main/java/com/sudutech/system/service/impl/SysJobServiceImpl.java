package com.sudutech.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysJob;
import com.sudutech.system.domain.SysJobLog;
import com.sudutech.system.domain.table.SysJobLogTableDef;
import com.sudutech.system.domain.table.SysJobTableDef;
import com.sudutech.system.mapper.SysJobLogMapper;
import com.sudutech.system.mapper.SysJobMapper;
import com.sudutech.system.quartz.QuartzJobExecution;
import com.sudutech.system.quartz.ScheduleConstants;
import com.sudutech.system.service.SysJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDataMap;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.sudutech.system.domain.table.SysJobTableDef.SYS_JOB;

/**
 * 定时任务服务实现（RAMJobStore + sys_job 持久化配置，重启后自动恢复）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysJobServiceImpl implements SysJobService {

    private final SysJobMapper jobMapper;
    private final SysJobLogMapper jobLogMapper;
    private final Scheduler scheduler;

    @Override
    public Page<SysJob> pageList(SysJob query, PageQuery pageQuery) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query != null) {
            if (StringUtils.isNotBlank(query.getJobName())) {
                wrapper.and(SYS_JOB.JOB_NAME.like(query.getJobName()));
            }
            if (StringUtils.isNotBlank(query.getJobGroup())) {
                wrapper.and(SYS_JOB.JOB_GROUP.eq(query.getJobGroup()));
            }
            if (StringUtils.isNotBlank(query.getStatus())) {
                wrapper.and(SYS_JOB.STATUS.eq(query.getStatus()));
            }
        }
        wrapper.orderBy(SYS_JOB.JOB_ID.asc());
        return jobMapper.paginate(pageQuery.toPage(), wrapper);
    }

    @Override
    public List<SysJob> selectAll() {
        return jobMapper.selectAll();
    }

    @Override
    public SysJob selectById(Long jobId) {
        return jobMapper.selectOneById(jobId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insert(SysJob job) {
        checkCron(job);
        job.setJobId(null);
        boolean ok = jobMapper.insert(job) > 0;
        if (ok && "0".equals(job.getStatus())) {
            createSchedule(job);
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysJob job) {
        SysJob exist = jobMapper.selectOneById(job.getJobId());
        if (exist == null) {
            throw new ServiceException("任务不存在");
        }
        checkCron(job);
        boolean ok = jobMapper.update(job) > 0;
        if (ok) {
            removeSchedule(exist);
            SysJob latest = jobMapper.selectOneById(job.getJobId());
            if ("0".equals(latest.getStatus())) {
                createSchedule(latest);
            }
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> jobIds) {
        int count = 0;
        for (Long jobId : jobIds) {
            SysJob job = jobMapper.selectOneById(jobId);
            if (job != null) {
                removeSchedule(job);
                count += jobMapper.deleteById(jobId);
            }
        }
        return count;
    }

    @Override
    public void run(Long jobId) {
        SysJob job = jobMapper.selectOneById(jobId);
        if (job == null) {
            throw new ServiceException("任务不存在");
        }
        JobKey jobKey = JobKey.jobKey("job_" + jobId, job.getJobGroup());
        try {
            JobDetail existDetail = scheduler.getJobDetail(jobKey);
            if (existDetail == null) {
                JobDetail detail = buildJobDetail(job);
                scheduler.addJob(detail, true);
            }
            scheduler.triggerJob(jobKey);
        } catch (Exception e) {
            throw new ServiceException("触发任务失败: " + e.getMessage());
        }
    }

    @Override
    public void changeStatus(Long jobId, String status) {
        SysJob exist = jobMapper.selectOneById(jobId);
        if (exist == null) {
            throw new ServiceException("任务不存在");
        }
        SysJob update = new SysJob();
        update.setJobId(jobId);
        update.setStatus(status);
        jobMapper.update(update);
        try {
            if ("0".equals(status)) {
                removeSchedule(exist);
                createSchedule(jobMapper.selectOneById(jobId));
            } else {
                scheduler.pauseJob(JobKey.jobKey("job_" + jobId, exist.getJobGroup()));
            }
        } catch (Exception e) {
            throw new ServiceException("变更任务状态失败: " + e.getMessage());
        }
    }

    @Override
    public void initSchedule() {
        List<SysJob> jobs = jobMapper.selectListByCondition(SYS_JOB.STATUS.eq("0"));
        int count = 0;
        for (SysJob job : jobs) {
            try {
                createSchedule(job);
                count++;
            } catch (Exception e) {
                log.warn("恢复定时任务失败: {}", job.getJobName(), e);
            }
        }
        log.info("定时任务初始化完成，共恢复 {} 个任务", count);
    }

    @Override
    public Page<SysJobLog> pageJobLog(SysJobLog query, PageQuery pageQuery) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query != null) {
            if (StringUtils.isNotBlank(query.getJobName())) {
                wrapper.and(SysJobLogTableDef.SYS_JOB_LOG.JOB_NAME.like(query.getJobName()));
            }
            if (StringUtils.isNotBlank(query.getStatus())) {
                wrapper.and(SysJobLogTableDef.SYS_JOB_LOG.STATUS.eq(query.getStatus()));
            }
        }
        wrapper.orderBy(SysJobLogTableDef.SYS_JOB_LOG.JOB_LOG_ID.desc());
        return jobLogMapper.paginate(pageQuery.toPage(), wrapper);
    }

    @Override
    public void clearJobLog() {
        jobLogMapper.deleteByQuery(com.mybatisflex.core.query.QueryWrapper.create());
    }

    private JobDetail buildJobDetail(SysJob job) {
        JobDataMap data = new JobDataMap();
        data.put(ScheduleConstants.JOB_DATA_KEY, job);
        return JobBuilder.newJob(QuartzJobExecution.class)
                .withIdentity("job_" + job.getJobId(), job.getJobGroup())
                .setJobData(data)
                .storeDurably()
                .build();
    }

    private void createSchedule(SysJob job) {
        try {
            JobDetail jobDetail = buildJobDetail(job);
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger_" + job.getJobId(), job.getJobGroup())
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression())
                            .withMisfireHandlingInstructionDoNothing())
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            if (scheduler.checkExists(TriggerKey.triggerKey("trigger_" + job.getJobId(), job.getJobGroup()))) {
                scheduler.resumeJob(JobKey.jobKey("job_" + job.getJobId(), job.getJobGroup()));
            }
        } catch (Exception e) {
            throw new ServiceException("创建定时任务失败: " + e.getMessage());
        }
    }

    private void removeSchedule(SysJob job) {
        try {
            scheduler.deleteJob(JobKey.jobKey("job_" + job.getJobId(), job.getJobGroup()));
        } catch (Exception e) {
            log.warn("移除定时任务失败: {}", job.getJobName(), e);
        }
    }

    private void checkCron(SysJob job) {
        if (StringUtils.isBlank(job.getCronExpression())) {
            throw new ServiceException("cron表达式不能为空");
        }
        try {
            CronScheduleBuilder.cronSchedule(job.getCronExpression());
        } catch (Exception e) {
            throw new ServiceException("cron表达式无效: " + job.getCronExpression());
        }
    }
}
