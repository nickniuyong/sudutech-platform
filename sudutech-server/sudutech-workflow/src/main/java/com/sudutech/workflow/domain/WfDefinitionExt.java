package com.sudutech.workflow.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程定义扩展表 wf_definition_ext
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("wf_definition_ext")
public class WfDefinitionExt extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String processKey;

    private String processName;

    private Integer version;

    private String businessType;

    private String formKey;

    private String category;

    /** BPMN 2.0 XML */
    private String bpmnXml;

    private String deploymentId;

    /** 0未部署 1已部署 */
    private String deployStatus;

    /** 0启用 1停用 */
    private String status;

    /** 非表字段 */
    @Column(ignore = true)
    private Long nodeCount;
}
