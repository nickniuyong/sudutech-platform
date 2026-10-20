package com.sudutech.workflow.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程操作日志表 wf_operation_log
 */
@Data
@Table("wf_operation_log")
public class WfOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String processInstanceId;

    private Long taskId;

    private String nodeKey;

    private Long operatorUserId;

    /** START/APPROVE/REJECT/DELEGATE/TRANSFER/CANCEL */
    private String operation;

    private String opinion;

    private LocalDateTime createTime;
}
