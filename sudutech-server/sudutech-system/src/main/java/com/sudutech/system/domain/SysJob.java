package com.sudutech.system.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务表 sys_job
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_job")
public class SysJob extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long jobId;

    private String jobName;

    private String jobGroup;

    /** 调用目标 springBean.method(args) */
    private String invokeTarget;

    private String cronExpression;

    /** 计划执行错误策略 1立即 2执行一次 3放弃 */
    private String misfirePolicy;

    /** 并发 0允许 1禁止 */
    private String concurrent;

    /** 0正常 1暂停 */
    private String status;

    /** 非表字段 */
    @Column(ignore = true)
    private String exceptionInfo;

    @Column(ignore = true)
    private String jobMessage;
}
