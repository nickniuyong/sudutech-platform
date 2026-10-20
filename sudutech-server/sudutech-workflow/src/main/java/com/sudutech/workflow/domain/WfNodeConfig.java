package com.sudutech.workflow.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程节点扩展配置表 wf_node_config
 */
@Data
@Table("wf_node_config")
public class WfNodeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long definitionId;

    private String nodeKey;

    private String nodeName;

    /** 简易引擎按此顺序流转 */
    private Integer nodeOrder;

    /** USER/ROLE/DEPT_LEADER/EXPRESSION */
    private String assigneeType;

    /** 审批人ID/角色key/表达式 */
    private String assigneeValue;

    /** ANY任一通过 ALL会签 */
    private String approveMode;

    /** 字段权限JSON {field: read/write/hidden} */
    @Column(ignore = true)
    private String formPermission;
}
