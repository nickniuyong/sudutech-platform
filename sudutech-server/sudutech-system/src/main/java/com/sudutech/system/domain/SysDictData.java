package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据表 sys_dict_data
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_dict_data")
public class SysDictData extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long dictCode;

    private Integer dictSort;

    private String dictLabel;

    private String dictValue;

    private String dictType;

    private String cssClass;

    /** Element 标签样式 success/danger/warning/info/primary */
    private String listClass;

    private String isDefault;

    private String status;
}
