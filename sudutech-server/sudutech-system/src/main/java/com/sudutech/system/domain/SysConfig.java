package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置表 sys_config
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_config")
public class SysConfig extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long configId;

    private String configName;

    private String configKey;

    private String configValue;

    /** Y系统内置 N自定义 */
    private String configType;
}
