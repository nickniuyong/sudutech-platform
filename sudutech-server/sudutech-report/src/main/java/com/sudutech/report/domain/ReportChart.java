package com.sudutech.report.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表图表配置表 report_chart
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("report_chart")
public class ReportChart extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long chartId;

    private Long pageId;

    private String chartName;

    /** BAR/LINE/PIE/TABLE/MAP/FUNNEL */
    private String chartType;

    private Long datasetId;

    /** ECharts option JSON */
    private String chartOption;

    @JsonProperty("xField")
    private String xField;

    @JsonProperty("yField")
    private String yField;

    /** 栅格宽 1-24 */
    private Integer width;

    private Integer height;

    private Integer sortNum;
}
