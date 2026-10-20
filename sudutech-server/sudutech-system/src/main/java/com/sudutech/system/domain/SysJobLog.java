package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务日志表 sys_job_log
 */
@Data
@Table("sys_job_log")
public class SysJobLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long jobLogId;

    private String jobName;

    private String jobGroup;

    private String invokeTarget;

    private String jobMessage;

    /** 0正常 1失败 */
    private String status;

    private String exceptionInfo;

    private LocalDateTime jobTime;
}
