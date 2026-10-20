package com.sudutech.ai.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI Schema 缓存表 ai_schema_cache
 */
@Data
@Table("ai_schema_cache")
public class AiSchemaCache implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long cacheId;

    private Long datasourceId;

    private String tableName;

    private String tableComment;

    /** [{name,type,comment}] */
    private String columnsJson;

    /** 脱敏样例 */
    private String sampleRows;

    private LocalDateTime refreshTime;
}
