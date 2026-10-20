package com.sudutech.gen.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码生成业务表字段 gen_table_column
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("gen_table_column")
public class GenTableColumn extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long columnId;

    private Long tableId;

    private String columnName;

    private String columnComment;

    private String columnType;

    private String javaType;

    private String javaField;

    private String isPk;

    private String isIncrement;

    private String isRequired;

    private String isInsert;

    private String isEdit;

    private String isList;

    private String isQuery;

    /** EQ/LIKE/BETWEEN/GT/LT/NE */
    private String queryType;

    /** input/textarea/select/radio/checkbox/datetime */
    private String htmlType;

    private String dictType;

    private Integer sortNum;

    /** 生成辅助：TableDef 常量名（如 USER_NAME） */
    @Column(ignore = true)
    private String defVar;

    /** 生成辅助：首字母大写 Java 字段名（如 UserName） */
    @Column(ignore = true)
    private String capField;

    /** 生成辅助：是否非空（information_schema 采集） */
    @Column(ignore = true)
    private String isNullable;
}
