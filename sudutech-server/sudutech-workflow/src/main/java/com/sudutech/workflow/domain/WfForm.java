package com.sudutech.workflow.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 动态表单表 wf_form（JSON Schema 渲染）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("wf_form")
public class WfForm extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String formName;

    private String formKey;

    /** 表单 Schema JSON */
    private String formContent;

    private String status;
}
