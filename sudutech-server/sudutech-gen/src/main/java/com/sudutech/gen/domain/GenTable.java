package com.sudutech.gen.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 代码生成业务表 gen_table
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("gen_table")
public class GenTable extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long tableId;

    private String tableName;

    private String tableComment;

    private String className;

    private String moduleName;

    private String businessName;

    private String functionName;

    private String authorName;

    /** crud/tree/sub */
    private String tplCategory;

    private String packageName;

    /** 0zip 1自定义路径 */
    private String genType;

    private String genPath;

    /** 其它选项 JSON */
    private String options;

    /** 非表字段 */
    @Column(ignore = true)
    private List<GenTableColumn> columns;
}
