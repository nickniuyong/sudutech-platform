package com.sudutech.screen.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 大屏项目表 screen_project
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "screen_project")
public class ScreenProject extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long projectId;

    private String projectName;

    private String projectCode;

    private String description;

    private String coverUrl;

    /** 0草稿 1已发布 */
    private String status;

    private String publishUrl;

    @Column(isLogicDelete = true)
    private String delFlag;
}
