package com.sudutech.gen.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.gen.domain.GenTable;
import com.sudutech.gen.service.GenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 代码生成
 */
@RestController
@RequestMapping("/api/v1/gen")
@RequiredArgsConstructor
public class GenController {

    private final GenService genService;

    /** 查询数据库表列表（用于导入） */
    @SaCheckPermission("gen:table:list")
    @GetMapping("/db/list")
    public R<List<Map<String, Object>>> dbList(@RequestParam(required = false) String tableName) {
        return R.ok(genService.selectDbTableList(tableName));
    }

    /** 导入表结构 */
    @SaCheckPermission("gen:table:import")
    @Log(title = "代码生成", businessType = BusinessType.IMPORT)
    @PostMapping("/table/sync")
    public R<Void> sync(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> tables = (List<String>) body.getOrDefault("tables", List.of());
        int count = genService.importTables(tables.toArray(new String[0]));
        return R.ok("成功导入 " + count + " 张表", null);
    }

    @SaCheckPermission("gen:table:list")
    @GetMapping("/table/list")
    public R<List<GenTable>> list(GenTable query,
                                  @RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(genService.pageList(query, pageNum, pageSize));
    }

    @SaCheckPermission("gen:table:query")
    @GetMapping("/table/{tableId}")
    public R<GenTable> getInfo(@PathVariable Long tableId) {
        return R.ok(genService.selectById(tableId));
    }

    @SaCheckPermission("gen:table:edit")
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @PutMapping("/table")
    public R<Void> edit(@RequestBody GenTable table) {
        genService.update(table);
        return R.ok("保存成功", null);
    }

    @SaCheckPermission("gen:table:remove")
    @Log(title = "代码生成", businessType = BusinessType.DELETE)
    @DeleteMapping("/table/{tableIds}")
    public R<Void> remove(@PathVariable Long[] tableIds) {
        genService.deleteByIds(tableIds);
        return R.ok("删除成功", null);
    }

    /** 在线预览生成代码 */
    @SaCheckPermission("gen:table:preview")
    @GetMapping("/preview/{tableId}")
    public R<Map<String, String>> preview(@PathVariable Long tableId) {
        return R.ok(genService.preview(tableId));
    }

    /** 批量生成代码 Zip 下载 */
    @SaCheckPermission("gen:table:genCode")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @PostMapping("/batchGenCode")
    public void batchGenCode(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) body.getOrDefault("tableIds", List.of());
        Long[] tableIds = ids.stream().map(Number::longValue).toArray(Long[]::new);
        byte[] data = genService.batchGenCode(tableIds);
        response.reset();
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" +
                URLEncoder.encode("sudutech-gen.zip", StandardCharsets.UTF_8));
        response.setContentLength(data.length);
        try {
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException("下载失败");
        }
    }
}
