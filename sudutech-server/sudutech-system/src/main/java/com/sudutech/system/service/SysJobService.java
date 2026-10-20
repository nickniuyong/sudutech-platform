package com.sudutech.system.service;

import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysJob;
import com.sudutech.system.domain.SysJobLog;

import java.util.List;

/**
 * 定时任务服务（Quartz，Bean 调用）
 */
public interface SysJobService {

    Page<SysJob> pageList(SysJob query, PageQuery pageQuery);

    List<SysJob> selectAll();

    SysJob selectById(Long jobId);

    boolean insert(SysJob job);

    boolean update(SysJob job);

    int deleteByIds(List<Long> jobIds);

    /** 立即执行一次 */
    void run(Long jobId);

    /** 变更状态 0启动 1暂停 */
    void changeStatus(Long jobId, String status);

    /** 应用启动时初始化调度 */
    void initSchedule();

    Page<SysJobLog> pageJobLog(SysJobLog query, PageQuery pageQuery);

    void clearJobLog();
}
