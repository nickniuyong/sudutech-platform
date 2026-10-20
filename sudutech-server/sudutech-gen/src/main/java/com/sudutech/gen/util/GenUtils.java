package com.sudutech.gen.util;

import com.sudutech.common.utils.StringUtils;
import com.sudutech.gen.domain.GenTable;
import com.sudutech.gen.domain.GenTableColumn;

/**
 * 代码生成辅助：Java 类型映射、命名推断、HTML 控件推断
 */
public class GenUtils {

    /** 数据库类型 -> Java 类型 */
    public static String columnToJava(String columnType) {
        String t = columnType == null ? "" : columnType.toLowerCase();
        if (t.contains("bigint")) {
            return "Long";
        }
        if (t.contains("int") || t.contains("tinyint") || t.contains("smallint") || t.contains("mediumint")) {
            return "Integer";
        }
        if (t.contains("float") || t.contains("double")) {
            return "Double";
        }
        if (t.contains("decimal") || t.contains("numeric")) {
            return "java.math.BigDecimal";
        }
        if (t.contains("bit") || t.contains("bool")) {
            return "Boolean";
        }
        if (t.contains("date") || t.contains("time") || t.contains("year")) {
            return "java.time.LocalDateTime";
        }
        if (t.contains("text") || t.contains("json")) {
            return "String";
        }
        return "String";
    }

    /** 表名推断：sys_user_config -> (SysUserConfig, user_config) */
    public static void initTable(GenTable table, String author) {
        String tableName = table.getTableName();
        String className = StringUtils.toCamelCase(tableName);
        // 去掉常见前缀
        for (String prefix : new String[]{"sys_", "wf_", "screen_", "report_", "ai_", "gen_", "biz_", "t_"}) {
            if (tableName.startsWith(prefix)) {
                className = StringUtils.toCamelCase(tableName.substring(prefix.length()));
                break;
            }
        }
        className = StringUtils.capitalize(className);
        table.setClassName(className);
        table.setBusinessName(StringUtils.uncapitalize(className));
        table.setModuleName(tableName.contains("_") ? tableName.substring(0, tableName.indexOf('_')) : "business");
        table.setFunctionName(StringUtils.isBlank(table.getTableComment())
                ? tableName : table.getTableComment());
        table.setAuthorName(author);
        table.setTplCategory("crud");
        table.setPackageName("com.sudutech.business");
        table.setGenType("0");
        table.setGenPath("/");
    }

    /** 字段初始化：Java类型/字段名/控件/查询方式 */
    public static void initColumn(GenTableColumn column, GenTable table) {
        String columnName = column.getColumnName();
        column.setJavaType(columnToJava(column.getColumnType()));
        String javaField = StringUtils.toCamelCase(columnName);
        column.setJavaField(javaField);
        column.setSortNum(column.getSortNum() == null ? 0 : column.getSortNum());

        boolean isPk = "1".equals(column.getIsPk());
        boolean isTime = "java.time.LocalDateTime".equals(column.getJavaType());
        boolean isStatusLike = columnName.endsWith("status") || columnName.endsWith("type")
                || columnName.endsWith("sex") || columnName.endsWith("flag");
        boolean isNameLike = columnName.endsWith("name") || columnName.endsWith("title")
                || columnName.endsWith("remark") || columnName.contains("content");
        boolean isDelFlag = "del_flag".equals(columnName) || columnName.endsWith("_by")
                || columnName.endsWith("_time");

        column.setIsRequired("0".equals(column.getIsNullable()) && !isPk ? "1" : "0");
        column.setIsInsert(isPk && "1".equals(column.getIsIncrement()) ? "0" : "1");
        column.setIsEdit(!isPk && !isDelFlag ? "1" : "0");
        column.setIsList(!isDelFlag ? "1" : "0");
        column.setIsQuery(columnName.endsWith("name") || isStatusLike ? "1" : "0");
        column.setQueryType(isNameLike ? "LIKE" : "EQ");

        if (isPk) {
            column.setHtmlType("");
        } else if (column.getColumnType() != null && column.getColumnType().toLowerCase().contains("text")) {
            column.setHtmlType("textarea");
        } else if (isTime) {
            column.setHtmlType("datetime");
        } else if (isStatusLike) {
            column.setHtmlType("select");
        } else {
            column.setHtmlType("input");
        }
        column.setDictType("");
    }

    private GenUtils() {
    }
}
