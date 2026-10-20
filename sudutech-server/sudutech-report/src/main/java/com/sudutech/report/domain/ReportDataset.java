package com.sudutech.report.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表数据集表 report_dataset
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("report_dataset")
public class ReportDataset extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long datasetId;

    private String datasetName;

    private Long datasourceId;

    /** 只读 SELECT */
    private String sqlText;

    /** 强制 LIMIT 防大查询 */
    private Integer rowLimit;

    /** 参数声明JSON */
    private String paramsSchema;
}
