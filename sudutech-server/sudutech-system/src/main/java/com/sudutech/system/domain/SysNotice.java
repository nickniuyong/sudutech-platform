package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告表 sys_notice
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_notice")
public class SysNotice extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long noticeId;

    private String noticeTitle;

    /** 1通知 2公告 */
    private String noticeType;

    private String noticeContent;

    private String status;
}
