package com.sudutech.workflow.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程任务运行表 wf_task（简化引擎运行时）
 */
@Data
@Table("wf_task")
public class WfTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String processInstanceId;

    private Long businessId;

    private String nodeKey;

    private String nodeName;

    private Long assigneeId;

    /** PENDING/DONE/CANCELED */
    private String status;

    /** APPROVE/REJECT */
    private String result;

    private String comment;

    private LocalDateTime createTime;

    private LocalDateTime finishTime;

    /** 非表字段 */
    @Column(ignore = true)
    private String assigneeName;

    @Column(ignore = true)
    private String businessTitle;

    @Column(ignore = true)
    private String applicantName;
}
