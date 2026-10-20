package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位表 sys_post
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_post")
public class SysPost extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long postId;

    private String postCode;

    private String postName;

    private Integer postSort;

    private String status;
}
