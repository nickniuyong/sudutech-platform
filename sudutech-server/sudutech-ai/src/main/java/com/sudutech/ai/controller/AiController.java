package com.sudutech.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.core.domain.TableDataInfo;
import com.sudutech.ai.domain.AiChatMessage;
import com.sudutech.ai.domain.AiChatSession;
import com.sudutech.ai.domain.AiDatasource;
import com.sudutech.ai.domain.AiSchemaCache;
import com.sudutech.ai.service.AiDatasourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 数据源与 NL2SQL
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiDatasourceService aiService;

    // ---------- 数据源管理 ----------
    @SaCheckPermission("ai:datasource:list")
    @GetMapping("/datasource/list")
    public R<List<AiDatasource>> datasourceList(@RequestParam(required = false) String name) {
        return R.ok(aiService.selectList(name));
    }

    @SaCheckPermission("ai:datasource:query")
    @GetMapping("/datasource/{id}")
    public R<AiDatasource> datasourceInfo(@PathVariable Long id) {
        return R.ok(aiService.selectById(id));
    }

    @SaCheckPermission("ai:datasource:add")
    @Log(title = "数据源管理", businessType = BusinessType.INSERT)
    @PostMapping("/datasource")
    public R<AiDatasource> saveDatasource(@RequestBody AiDatasource datasource) {
        return R.ok("保存成功", aiService.save(datasource));
    }

    @SaCheckPermission("ai:datasource:remove")
    @Log(title = "数据源管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/datasource/{ids}")
    public R<Void> removeDatasource(@PathVariable List<Long> ids) {
        aiService.deleteByIds(ids);
        return R.ok("删除成功", null);
    }

    @SaCheckPermission("ai:datasource:query")
    @PostMapping("/datasource/test")
    public R<Void> testConnection(@RequestBody AiDatasource datasource) {
        aiService.testConnection(datasource);
        return R.ok("连接成功", null);
    }

    /** 重新采集表结构 */
    @SaCheckPermission("ai:datasource:refresh")
    @Log(title = "Schema采集", businessType = BusinessType.UPDATE)
    @PostMapping("/schema/refresh/{datasourceId}")
    public R<Integer> refreshSchema(@PathVariable Long datasourceId) {
        int count = aiService.refreshSchema(datasourceId);
        return R.ok("已采集 " + count + " 张表", count);
    }

    @SaCheckPermission("ai:datasource:query")
    @GetMapping("/schema/list/{datasourceId}")
    public R<List<AiSchemaCache>> schemaList(@PathVariable Long datasourceId) {
        return R.ok(aiService.schemaCacheList(datasourceId));
    }

    // ---------- 会话 ----------
    @SaCheckPermission("ai:chat:list")
    @GetMapping("/chat/session/list")
    public TableDataInfo<AiChatSession> sessions(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "50") int pageSize) {
        Page<AiChatSession> page = aiService.sessionPage(pageNum, pageSize);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckPermission("ai:chat:list")
    @PostMapping("/chat/session")
    public R<AiChatSession> createSession(@RequestBody(required = false) Map<String, Object> body) {
        Long datasourceId = body == null || body.get("datasourceId") == null
                ? null : Long.valueOf(String.valueOf(body.get("datasourceId")));
        return R.ok(aiService.createSession(datasourceId));
    }

    @SaCheckPermission("ai:chat:list")
    @DeleteMapping("/chat/session/{sessionId}")
    public R<Void> deleteSession(@PathVariable Long sessionId) {
        aiService.deleteSession(sessionId);
        return R.ok("删除成功", null);
    }

    /** 历史消息 */
    @SaCheckPermission("ai:chat:list")
    @GetMapping("/chat/message/{sessionId}")
    public TableDataInfo<AiChatMessage> messages(@PathVariable Long sessionId) {
        Page<AiChatMessage> page = aiService.messagePage(sessionId, 1, 200);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    /** 发送问题：自然语言 -> SQL -> 执行结果 */
    @SaCheckPermission("ai:chat:list")
    @Log(title = "AI问数", businessType = BusinessType.OTHER, saveParam = false)
    @PostMapping("/chat/message")
    public R<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        Long sessionId = body.get("sessionId") == null ? null
                : Long.valueOf(String.valueOf(body.get("sessionId")));
        Long datasourceId = body.get("datasourceId") == null ? null
                : Long.valueOf(String.valueOf(body.get("datasourceId")));
        String question = String.valueOf(body.getOrDefault("question", ""));
        if (question.isBlank()) {
            return R.fail("问题不能为空");
        }
        return R.ok(aiService.chat(sessionId, datasourceId, question));
    }
}
