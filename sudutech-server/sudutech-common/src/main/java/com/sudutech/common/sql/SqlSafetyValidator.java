package com.sudutech.common.sql;

import com.sudutech.common.exception.ServiceException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.Locale;

/**
 * SQL 安全校验：只放行单条 SELECT，拦截 DDL/DML/多语句/危险函数
 */
public class SqlSafetyValidator {

    /** 禁止出现的关键字（注释注入、文件读写等） */
    private static final String[] FORBIDDEN_KEYWORDS = {
            "into outfile", "into dumpfile", "load_file", "outfile",
            "information_schema.mouse", "sleep(", "benchmark(", "load data"
    };

    public static void validateSelectOnly(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new ServiceException("SQL语句不能为空");
        }
        String normalized = sql.trim();
        // 多语句拦截
        if (normalized.contains(";") && !normalized.endsWith(";")) {
            throw new ServiceException("不允许执行多条SQL语句");
        }
        if (normalized.endsWith(";")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (lower.contains(keyword)) {
                throw new ServiceException("SQL包含被禁止的关键字: " + keyword);
            }
        }
        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(normalized);
        } catch (Exception e) {
            throw new ServiceException("SQL语法解析失败: " + e.getMessage());
        }
        if (!(statement instanceof Select)) {
            throw new ServiceException("仅允许执行 SELECT 查询语句");
        }
    }

    private SqlSafetyValidator() {
    }
}
