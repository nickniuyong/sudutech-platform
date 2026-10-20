package com.sudutech.ai.service;

import com.sudutech.ai.domain.AiDatasource;
import com.sudutech.ai.domain.AiSchemaCache;
import com.sudutech.common.sql.SqlResult;

import java.util.List;
import java.util.Map;

/**
 * AI 数据源与 Schema 服务
 */
public interface AiDatasourceService {

    List<AiDatasource> selectList(String name);

    AiDatasource selectById(Long datasourceId);

    AiDatasource save(AiDatasource datasource);

    int deleteByIds(List<Long> ids);

    /** 测试连接 */
    void testConnection(AiDatasource datasource);

    /** 重新采集库表结构（写入 ai_schema_cache，样例数据脱敏） */
    int refreshSchema(Long datasourceId);

    List<AiSchemaCache> schemaCacheList(Long datasourceId);

    /** 获取可用数据源连接（供 SQL 执行） */
    javax.sql.DataSource getDataSource(Long datasourceId);

    /** NL2SQL: 自然语言 -> {sql, chartType, summary}，并执行返回结果 */
    Map<String, Object> chat(Long sessionId, Long datasourceId, String question);

    com.mybatisflex.core.paginate.Page<com.sudutech.ai.domain.AiChatSession> sessionPage(int pageNum, int pageSize);

    com.mybatisflex.core.paginate.Page<com.sudutech.ai.domain.AiChatMessage> messagePage(Long sessionId, int pageNum, int pageSize);

    com.sudutech.ai.domain.AiChatSession createSession(Long datasourceId);

    int deleteSession(Long sessionId);

    SqlResult executeGeneratedSql(Long datasourceId, String sql, Map<String, Object> params);
}
