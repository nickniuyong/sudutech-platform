package com.sudutech.ai.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.ai.client.ArkClient;
import com.sudutech.ai.domain.AiChatMessage;
import com.sudutech.ai.domain.AiChatSession;
import com.sudutech.ai.domain.AiDatasource;
import com.sudutech.ai.domain.AiSchemaCache;
import com.sudutech.ai.mapper.AiChatMessageMapper;
import com.sudutech.ai.mapper.AiChatSessionMapper;
import com.sudutech.ai.mapper.AiDatasourceMapper;
import com.sudutech.ai.mapper.AiSchemaCacheMapper;
import com.sudutech.ai.service.AiDatasourceService;
import com.sudutech.common.config.CryptoService;
import com.sudutech.common.core.domain.LoginUser;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.sql.DynamicDataSourceManager;
import com.sudutech.common.sql.SqlExecutor;
import com.sudutech.common.sql.SqlResult;
import com.sudutech.common.sql.SqlSafetyValidator;
import com.sudutech.common.utils.JsonUtils;
import com.sudutech.common.utils.LoginHelper;
import com.sudutech.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sudutech.ai.domain.table.AiChatSessionTableDef.AI_CHAT_SESSION;
import static com.sudutech.ai.domain.table.AiDatasourceTableDef.AI_DATASOURCE;
import static com.sudutech.ai.domain.table.AiSchemaCacheTableDef.AI_SCHEMA_CACHE;

/**
 * AI 数据源与 NL2SQL 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiDatasourceServiceImpl implements AiDatasourceService {

    private final AiDatasourceMapper datasourceMapper;
    private final AiSchemaCacheMapper schemaCacheMapper;
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final SqlExecutor sqlExecutor;
    private final DynamicDataSourceManager dataSourceManager;
    private final CryptoService cryptoService;
    private final ArkClient arkClient;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<AiDatasource> selectList(String name) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (StringUtils.isNotBlank(name)) {
            wrapper.and(AI_DATASOURCE.DATASOURCE_NAME.like(name));
        }
        wrapper.orderBy(AI_DATASOURCE.DATASOURCE_ID.desc());
        List<AiDatasource> list = datasourceMapper.selectListByQuery(wrapper);
        list.forEach(d -> d.setPassword(null));
        return list;
    }

    @Override
    public AiDatasource selectById(Long datasourceId) {
        AiDatasource ds = datasourceMapper.selectOneById(datasourceId);
        if (ds != null) {
            ds.setPassword(null);
        }
        return ds;
    }

    @Override
    public AiDatasource save(AiDatasource datasource) {
        if (datasource.getDatasourceId() == null) {
            encodePassword(datasource, null);
            datasource.setDatasourceId(null);
            datasourceMapper.insert(datasource);
        } else {
            AiDatasource old = datasourceMapper.selectOneById(datasource.getDatasourceId());
            encodePassword(datasource, old);
            // 密码留空表示不修改
            if (StringUtils.isBlank(datasource.getPassword())) {
                datasource.setPassword(null);
            }
            datasourceMapper.update(datasource);
        }
        AiDatasource saved = datasourceMapper.selectOneById(datasource.getDatasourceId());
        saved.setPassword(null);
        return saved;
    }

    private void encodePassword(AiDatasource datasource, AiDatasource old) {
        if (datasource.getPlainPassword() != null && !datasource.getPlainPassword().isEmpty()) {
            datasource.setPassword(cryptoService.encrypt(datasource.getPlainPassword()));
        } else if (datasource.getPassword() != null && !datasource.getPassword().isEmpty()
                && !datasource.getPassword().startsWith("{AES}")) {
            // 兼容明文提交：加密存储
            datasource.setPassword(cryptoService.encrypt(datasource.getPassword()));
        } else if (old != null && StringUtils.isBlank(datasource.getPassword())) {
            datasource.setPassword(old.getPassword());
        }
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            schemaCacheMapper.deleteByCondition(AI_SCHEMA_CACHE.DATASOURCE_ID.eq(id));
            dataSourceManager.remove(id);
            count += datasourceMapper.deleteById(id);
        }
        return count;
    }

    @Override
    public void testConnection(AiDatasource datasource) {
        String password = StringUtils.isNotBlank(datasource.getPlainPassword())
                ? datasource.getPlainPassword()
                : cryptoService.decrypt(datasource.getPassword());
        sqlExecutor.testConnection(datasource.getJdbcUrl(), datasource.getUsername(),
                password, driverClass(datasource.getDbType()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refreshSchema(Long datasourceId) {
        AiDatasource ds = datasourceMapper.selectOneById(datasourceId);
        if (ds == null) {
            throw new ServiceException("数据源不存在");
        }
        String dbName = StringUtils.isNotBlank(ds.getDbName()) ? ds.getDbName() : null;
        String sql = "select table_name, table_comment from information_schema.tables " +
                "where table_schema = ? and table_type = 'BASE TABLE'";
        if (dbName == null) {
            sql = sql.replace("table_schema = ?", "table_schema = database()");
            dbName = "%";
        }
        List<Map<String, Object>> tables = queryOnDatasource(ds, sql, dbName == null ? new Object[0] : new Object[]{dbName});

        schemaCacheMapper.deleteByCondition(AI_SCHEMA_CACHE.DATASOURCE_ID.eq(datasourceId));
        int count = 0;
        for (Map<String, Object> table : tables) {
            String tableName = String.valueOf(table.get("table_name") != null
                    ? table.get("table_name") : table.get("TABLE_NAME"));
            String comment = String.valueOf(table.get("table_comment") != null
                    ? table.get("table_comment") : table.get("TABLE_COMMENT"));

            String colSql = "select column_name, data_type, column_comment from information_schema.columns " +
                    "where table_schema = " + (StringUtils.isNotBlank(ds.getDbName()) ? "?" : "database()")
                    + " and table_name = ? order by ordinal_position";
            List<Map<String, Object>> cols;
            if (StringUtils.isNotBlank(ds.getDbName())) {
                cols = queryOnDatasource(ds, colSql, new Object[]{ds.getDbName(), tableName});
            } else {
                cols = queryOnDatasource(ds, colSql, new Object[]{tableName});
            }
            List<Map<String, Object>> colList = new ArrayList<>();
            for (Map<String, Object> col : cols) {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("name", col.get("column_name") != null ? col.get("column_name") : col.get("COLUMN_NAME"));
                c.put("type", col.get("data_type") != null ? col.get("data_type") : col.get("DATA_TYPE"));
                c.put("comment", col.get("column_comment") != null ? col.get("column_comment") : col.get("COLUMN_COMMENT"));
                colList.add(c);
            }

            AiSchemaCache cache = new AiSchemaCache();
            cache.setDatasourceId(datasourceId);
            cache.setTableName(tableName);
            cache.setTableComment(comment);
            cache.setColumnsJson(JsonUtils.toJson(colList));
            cache.setSampleRows(sampleRows(ds, tableName));
            cache.setRefreshTime(LocalDateTime.now());
            schemaCacheMapper.insert(cache);
            count++;
        }
        return count;
    }

    /** 采样前3行并脱敏：文本字符替换为* */
    private String sampleRows(AiDatasource ds, String tableName) {
        try {
            List<Map<String, Object>> rows = queryOnDatasource(ds,
                    "select * from `" + tableName.replace("`", "") + "` limit 3", new Object[0]);
            for (Map<String, Object> row : rows) {
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    Object v = entry.getValue();
                    if (v instanceof String s && s.length() > 0) {
                        String masked = s.length() <= 3 ? "*".repeat(s.length())
                                : s.charAt(0) + "*".repeat(Math.min(s.length() - 2, 8)) + s.charAt(s.length() - 1);
                        entry.setValue(masked);
                    }
                }
            }
            return JsonUtils.toJson(rows);
        } catch (Exception e) {
            log.warn("采样失败 table={}: {}", tableName, e.getMessage());
            return "[]";
        }
    }

    private List<Map<String, Object>> queryOnDatasource(AiDatasource ds, String sql, Object[] args) {
        try (Connection conn = getRawConnection(ds);
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            }
        } catch (Exception e) {
            throw new ServiceException("读取元数据失败: " + e.getMessage());
        }
    }

    private Connection getRawConnection(AiDatasource ds) {
        try {
            String password = cryptoService.decrypt(ds.getPassword());
            Class.forName(driverClass(ds.getDbType()));
            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", ds.getUsername() == null ? "" : ds.getUsername());
            props.setProperty("password", password == null ? "" : password);
            return java.sql.DriverManager.getConnection(ds.getJdbcUrl(), props);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("连接数据源失败: " + e.getMessage());
        }
    }

    private String driverClass(String dbType) {
        return switch (dbType == null ? "mysql" : dbType) {
            case "mysql" -> "com.mysql.cj.jdbc.Driver";
            case "pg" -> "org.postgresql.Driver";
            case "clickhouse" -> "com.clickhouse.jdbc.ClickHouseDriver";
            default -> throw new ServiceException("暂不支持的数据库类型: " + dbType);
        };
    }

    @Override
    public List<AiSchemaCache> schemaCacheList(Long datasourceId) {
        return schemaCacheMapper.selectListByCondition(AI_SCHEMA_CACHE.DATASOURCE_ID.eq(datasourceId));
    }

    @Override
    public DataSource getDataSource(Long datasourceId) {
        AiDatasource ds = datasourceMapper.selectOneById(datasourceId);
        if (ds == null) {
            throw new ServiceException("数据源不存在");
        }
        String password = cryptoService.decrypt(ds.getPassword());
        return dataSourceManager.getPool(datasourceId, ds.getJdbcUrl(), ds.getUsername(),
                password, driverClass(ds.getDbType()));
    }

    // ---------------- NL2SQL ----------------

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是一个专业的SQL生成助手。根据用户问题和数据库表结构，生成一条MySQL查询SQL。

            表结构（表名|注释|字段列表）：
            %s

            严格要求：
            1. 只生成一条 SELECT 查询语句，禁止 INSERT/UPDATE/DELETE/DDL/DML，禁止 INTO OUTFILE 等危险操作
            2. SQL 使用 MySQL 方言，表名和字段名必须来自上面的表结构
            3. 需要聚合统计时优先使用 GROUP BY，并对数值列合理命名别名（中文别名加反引号）
            4. 结果行数控制在 1000 行以内（必要时加 LIMIT）
            5. 严格输出如下JSON（不要输出多余文字，不要用markdown代码块包裹）：
            {"sql":"SELECT ...","chartType":"BAR|LINE|PIE|TABLE|MAP|FUNNEL|gauge","summary":"一句话说明查询内容"}
            """;

    @Override
    public Map<String, Object> chat(Long sessionId, Long datasourceId, String question) {
        LoginUser loginUser = LoginHelper.getLoginUserRequired();
        AiChatSession session = sessionId == null ? createSessionInternal(datasourceId, loginUser.getUserId())
                : sessionMapper.selectOneById(sessionId);
        if (session == null) {
            throw new ServiceException("会话不存在");
        }
        Long dsId = datasourceId != null ? datasourceId : session.getDatasourceId();
        if (dsId == null) {
            throw new ServiceException("请先选择数据源");
        }

        // 保存用户消息
        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setSessionId(session.getSessionId());
        userMessage.setRole("user");
        userMessage.setContent(question);
        userMessage.setStatus("OK");
        userMessage.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMessage);

        // 首条消息作为会话标题
        if ("新会话".equals(session.getSessionTitle()) || StringUtils.isBlank(session.getSessionTitle())) {
            AiChatSession upd = new AiChatSession();
            upd.setSessionId(session.getSessionId());
            upd.setSessionTitle(question.length() > 30 ? question.substring(0, 30) : question);
            upd.setDatasourceId(dsId);
            upd.setUpdateTime(LocalDateTime.now());
            sessionMapper.update(upd);
        }

        // 拉取 Schema 并拼装 Prompt
        List<AiSchemaCache> caches = schemaCacheList(dsId);
        if (caches.isEmpty()) {
            throw new ServiceException("该数据源尚未采集表结构，请先执行 Schema 采集");
        }
        StringBuilder schemaText = new StringBuilder();
        for (AiSchemaCache cache : caches) {
            schemaText.append("- ").append(cache.getTableName());
            if (StringUtils.isNotBlank(cache.getTableComment())) {
                schemaText.append("（").append(cache.getTableComment()).append("）");
            }
            schemaText.append(": ");
            List<Map<String, Object>> cols = JsonUtils.parseMapList(cache.getColumnsJson());
            if (cols != null) {
                List<String> parts = new ArrayList<>();
                for (Map<String, Object> col : cols) {
                    parts.add(col.get("name") + " " + col.get("type")
                            + (col.get("comment") != null && !String.valueOf(col.get("comment")).isEmpty()
                            ? "(" + col.get("comment") + ")" : ""));
                }
                schemaText.append(String.join(", ", parts));
            }
            schemaText.append("\n");
        }

        String answer = arkClient.chat(
                String.format(SYSTEM_PROMPT_TEMPLATE, schemaText),
                question);

        // 解析 JSON {sql, chartType, summary}
        String sql = null;
        String chartType = "TABLE";
        String summary = answer;
        try {
            String json = extractJson(answer);
            Map<String, Object> parsed = JsonUtils.parseMap(json);
            if (parsed != null && parsed.get("sql") != null) {
                sql = String.valueOf(parsed.get("sql"));
                chartType = parsed.get("chartType") != null ? String.valueOf(parsed.get("chartType")) : "TABLE";
                summary = parsed.get("summary") != null ? String.valueOf(parsed.get("summary")) : "";
            }
        } catch (Exception e) {
            log.warn("解析大模型JSON失败: {}", answer);
        }

        AiChatMessage assistantMessage = new AiChatMessage();
        assistantMessage.setSessionId(session.getSessionId());
        assistantMessage.setRole("assistant");
        assistantMessage.setCreateTime(LocalDateTime.now());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", session.getSessionId());
        result.put("chartType", chartType);

        if (sql == null) {
            // 非SQL回答（如闲聊/解释）
            assistantMessage.setContent(answer);
            assistantMessage.setStatus("OK");
            messageMapper.insert(assistantMessage);
            result.put("summary", answer);
            result.put("sql", null);
            return result;
        }

        try {
            SqlSafetyValidator.validateSelectOnly(sql);
            SqlResult sqlResult = executeGeneratedSql(dsId, sql, Map.of());
            String chartData = JsonUtils.toJson(Map.of(
                    "columns", sqlResult.getColumns() == null ? List.of() : sqlResult.getColumns(),
                    "rows", sqlResult.getRows() == null ? List.of() : sqlResult.getRows()));
            assistantMessage.setContent(summary);
            assistantMessage.setGeneratedSql(sql);
            assistantMessage.setChartType(chartType);
            assistantMessage.setChartData(chartData);
            assistantMessage.setStatus("OK");
            messageMapper.insert(assistantMessage);

            result.put("summary", summary);
            result.put("sql", sql);
            result.put("columns", sqlResult.getColumns());
            result.put("rows", sqlResult.getRows());
        } catch (Exception e) {
            assistantMessage.setContent("SQL执行失败: " + e.getMessage() + "\n生成的SQL: " + sql);
            assistantMessage.setGeneratedSql(sql);
            assistantMessage.setStatus("ERROR");
            messageMapper.insert(assistantMessage);
            throw new ServiceException("SQL执行失败: " + e.getMessage() + "（生成SQL: " + sql + "）");
        }
        return result;
    }

    private String extractJson(String text) {
        if (text == null) {
            throw new ServiceException("大模型返回为空");
        }
        String t = text.trim();
        if (t.contains("```")) {
            int start = t.indexOf("```");
            int end = t.lastIndexOf("```");
            if (end > start) {
                t = t.substring(start + 3, end).trim();
                if (t.startsWith("json")) {
                    t = t.substring(4).trim();
                }
            }
        }
        int braceStart = t.indexOf('{');
        int braceEnd = t.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return t.substring(braceStart, braceEnd + 1);
        }
        throw new ServiceException("大模型未返回有效JSON");
    }

    @Override
    public SqlResult executeGeneratedSql(Long datasourceId, String sql, Map<String, Object> params) {
        return sqlExecutor.execute(getDataSource(datasourceId), sql, params, 1000);
    }

    @Override
    public Page<AiChatSession> sessionPage(int pageNum, int pageSize) {
        LoginUser loginUser = LoginHelper.getLoginUserRequired();
        return sessionMapper.paginate(Page.of(pageNum, pageSize),
                QueryWrapper.create()
                        .where(AI_CHAT_SESSION.USER_ID.eq(loginUser.getUserId()))
                        .orderBy(AI_CHAT_SESSION.UPDATE_TIME.desc()));
    }

    @Override
    public Page<AiChatMessage> messagePage(Long sessionId, int pageNum, int pageSize) {
        return messageMapper.paginate(Page.of(pageNum, pageSize),
                QueryWrapper.create()
                        .where(com.sudutech.ai.domain.table.AiChatMessageTableDef.AI_CHAT_MESSAGE.SESSION_ID.eq(sessionId))
                        .orderBy(com.sudutech.ai.domain.table.AiChatMessageTableDef.AI_CHAT_MESSAGE.MESSAGE_ID.asc()));
    }

    @Override
    public AiChatSession createSession(Long datasourceId) {
        LoginUser loginUser = LoginHelper.getLoginUserRequired();
        return createSessionInternal(datasourceId, loginUser.getUserId());
    }

    private AiChatSession createSessionInternal(Long datasourceId, Long userId) {
        AiChatSession session = new AiChatSession();
        session.setSessionTitle("新会话");
        session.setDatasourceId(datasourceId);
        session.setUserId(userId);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    @Override
    public int deleteSession(Long sessionId) {
        AiChatSession session = sessionMapper.selectOneById(sessionId);
        if (session == null) {
            throw new ServiceException("会话不存在");
        }
        messageMapper.deleteByCondition(
                com.sudutech.ai.domain.table.AiChatMessageTableDef.AI_CHAT_MESSAGE.SESSION_ID.eq(sessionId));
        return sessionMapper.deleteById(sessionId);
    }
}
