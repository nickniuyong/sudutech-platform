package com.sudutech.report.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表页表 report_page
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "report_page")
public class ReportPage extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long pageId;

    private String pageName;

    private String pageCode;

    /** 栅格布局JSON */
    private String layoutConfig;

    private String status;

    @Column(isLogicDelete = true)
    private String delFlag;
}
