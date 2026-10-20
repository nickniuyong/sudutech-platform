package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型表 sys_dict_type
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_dict_type")
public class SysDictType extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long dictId;

    private String dictName;

    private String dictType;

    private String status;
}
