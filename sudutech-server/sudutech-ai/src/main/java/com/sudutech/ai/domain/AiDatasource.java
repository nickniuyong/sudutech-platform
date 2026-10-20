package com.sudutech.ai.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 数据源表 ai_datasource
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("ai_datasource")
public class AiDatasource extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long datasourceId;

    private String datasourceName;

    /** mysql/pg/clickhouse */
    private String dbType;

    private String jdbcUrl;

    private String username;

    /** AES加密存储 */
    private String password;

    /** NL2SQL 默认库 */
    private String dbName;

    private String readonlyUser;

    private String status;

    /** 非表字段：明文密码传输用（不落库） */
    @com.mybatisflex.annotation.Column(ignore = true)
    private transient String plainPassword;
}
