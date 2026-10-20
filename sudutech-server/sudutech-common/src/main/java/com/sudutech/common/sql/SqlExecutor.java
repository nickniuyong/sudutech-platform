package com.sudutech.common.sql;

import com.sudutech.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 轻量 JDBC 只读执行器：
 * 1. JSqlParser 校验只允许 SELECT
 * 2. :param 占位符替换为 ? 并参数绑定
 * 3. 强制追加 LIMIT 保护数据库
 */
@Slf4j
@Component
public class SqlExecutor {

    /** :paramName 占位符（排除 :: 类型转换与字符串内的冒号） */
    private static final Pattern PARAM_PATTERN = Pattern.compile("(?<![:@\\w]):([a-zA-Z_][a-zA-Z0-9_]*)");

    private static final String LIMIT_CLAUSE_PATTERN = "(?i)\\bLIMIT\\s+\\d+(\\s*,\\s*\\d+)?\\s*$";

    /**
     * 执行只读查询
     *
     * @param sql      SQL语句，支持 :paramName 占位符
     * @param params   参数名 -> 值
     * @param maxRows  强制行数上限
     */
    public SqlResult execute(DataSource dataSource, String sql, Map<String, Object> params, int maxRows) {
        SqlSafetyValidator.validateSelectOnly(sql);
        int limit = maxRows > 0 ? maxRows : 1000;

        String executable = sql.trim();
        if (executable.endsWith(";")) {
            executable = executable.substring(0, executable.length() - 1);
        }

        // 占位符替换 + 参数收集
        List<Object> bindArgs = new ArrayList<>();
        Matcher matcher = PARAM_PATTERN.matcher(executable);
        StringBuilder replaced = new StringBuilder();
        Map<String, Object> safeParams = params == null ? Map.of() : params;
        while (matcher.find()) {
            String name = matcher.group(1);
            if (!safeParams.containsKey(name)) {
                throw new ServiceException("缺少查询参数: :" + name);
            }
            bindArgs.add(safeParams.get(name));
            matcher.appendReplacement(replaced, "?");
        }
        matcher.appendTail(replaced);
        executable = replaced.toString();

        // 无 LIMIT 时强制追加
        boolean limitApplied = false;
        if (!executable.matches("(?s).*" + LIMIT_CLAUSE_PATTERN)) {
            executable = executable + " LIMIT " + limit;
            limitApplied = true;
        } else {
            // 已有LIMIT也做上限保护
            executable = executable + " LIMIT " + limit;
            limitApplied = true;
        }

        long start = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(executable)) {
            ps.setMaxRows(limit);
            for (int i = 0; i < bindArgs.size(); i++) {
                ps.setObject(i + 1, bindArgs.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    columns.add(meta.getColumnLabel(i));
                }
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        Object val = rs.getObject(i);
                        if (val instanceof byte[] bytes) {
                            val = new String(bytes);
                        }
                        row.put(columns.get(i - 1), val);
                    }
                    rows.add(row);
                }
                SqlResult result = new SqlResult();
                result.setColumns(columns);
                result.setRows(rows);
                result.setCostTime(System.currentTimeMillis() - start);
                result.setLimitApplied(limitApplied);
                return result;
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("SQL执行失败: {}", executable, e);
            throw new ServiceException("SQL执行失败: " + e.getMessage());
        }
    }

    /** 测试连接是否可用 */
    public void testConnection(String jdbcUrl, String username, String password, String driverClass) {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new ServiceException("未找到JDBC驱动: " + driverClass + "，请在运行环境引入对应驱动包");
        }
        try (Connection conn = buildDirectConnection(jdbcUrl, username, password, driverClass)) {
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT 1")) {
                if (!rs.next()) {
                    throw new ServiceException("连接测试失败");
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("连接失败: " + e.getMessage());
        }
    }

    private Connection buildDirectConnection(String url, String user, String pass, String driver)
            throws Exception {
        java.util.Properties props = new java.util.Properties();
        if (user != null) {
            props.setProperty("user", user);
        }
        if (pass != null) {
            props.setProperty("password", pass);
        }
        props.setProperty("connectTimeout", "5000");
        return java.sql.DriverManager.getConnection(url, props);
    }
}
