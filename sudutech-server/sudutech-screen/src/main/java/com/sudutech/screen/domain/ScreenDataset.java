package com.sudutech.screen.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 大屏数据集表 screen_dataset
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("screen_dataset")
public class ScreenDataset extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long datasetId;

    private String datasetName;

    private Long projectId;

    /** 指向 ai_datasource */
    private Long datasourceId;

    /** SQL 支持 :param 占位符 */
    private String sqlText;

    /** 参数声明JSON */
    private String paramsSchema;

    /** 自动刷新秒数 0=不刷新 */
    private Integer refreshInterval;
}
