package com.sudutech.workflow.service;

import com.mybatisflex.core.paginate.Page;
import com.sudutech.workflow.domain.WfBusiness;
import com.sudutech.workflow.domain.WfOperationLog;
import com.sudutech.workflow.domain.WfTask;

import java.util.List;

/**
 * 流程实例服务（简化顺序审批引擎）
 */
public interface WfInstanceService {

    /** 启动流程：创建实例关联 + 首节点任务 */
    WfBusiness start(String processKey, String businessId, String businessTitle, String formData);

    /** 审批通过 */
    void approve(Long taskId, String comment);

    /** 驳回 */
    void reject(Long taskId, String comment);

    /** 委派 */
    void delegate(Long taskId, Long targetUserId, String comment);

    Page<WfTask> todoTasks(int pageNum, int pageSize);

    Page<WfTask> doneTasks(int pageNum, int pageSize);

    Page<WfBusiness> myInstances(int pageNum, int pageSize);

    /** 流程轨迹 */
    List<WfOperationLog> trace(String processInstanceId);

    List<WfTask> instanceTasks(String processInstanceId);

    WfBusiness selectInstance(Long businessId);
}
