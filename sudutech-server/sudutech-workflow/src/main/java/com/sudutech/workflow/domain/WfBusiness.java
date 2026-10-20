package com.sudutech.workflow.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 业务流程实例关联表 wf_business
 */
@Data
@Table("wf_business")
public class WfBusiness implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String processKey;

    private String processInstanceId;

    private String businessId;

    private String businessTitle;

    /** DRAFT/RUNNING/APPROVED/REJECTED/CANCELED */
    private String status;

    private String currentNode;

    private Long applicantUserId;

    /** 表单数据JSON */
    private String formData;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 非表字段 */
    @Column(ignore = true)
    private String applicantName;

    @Column(ignore = true)
    private String processName;
}
