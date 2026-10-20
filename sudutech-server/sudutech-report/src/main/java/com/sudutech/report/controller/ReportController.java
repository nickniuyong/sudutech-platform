package com.sudutech.report.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.sql.SqlResult;
import com.sudutech.report.domain.ReportChart;
import com.sudutech.report.domain.ReportDataset;
import com.sudutech.report.domain.ReportPage;
import com.sudutech.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 报表管理
 */
@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ---------- 报表页 ----------
    @SaCheckPermission("report:page:list")
    @GetMapping("/page/list")
    public R<List<ReportPage>> pageList(@RequestParam(required = false) String pageName) {
        return R.ok(reportService.pageList(pageName));
    }

    @SaCheckPermission("report:page:list")
    @Log(title = "报表页", businessType = BusinessType.INSERT)
    @PostMapping("/page")
    public R<ReportPage> savePage(@RequestBody ReportPage page) {
        return R.ok("保存成功", reportService.savePage(page));
    }

    @SaCheckPermission("report:page:list")
    @Log(title = "报表页", businessType = BusinessType.DELETE)
    @DeleteMapping("/page/{pageIds}")
    public R<Void> removePages(@PathVariable List<Long> pageIds) {
        reportService.deletePages(pageIds);
        return R.ok("删除成功", null);
    }

    // ---------- 数据集 ----------
    @SaCheckPermission("report:page:list")
    @GetMapping("/dataset/list")
    public R<List<ReportDataset>> datasetList(@RequestParam(required = false) String name) {
        return R.ok(reportService.datasetList(name));
    }

    @SaCheckPermission("report:page:list")
    @Log(title = "报表数据集", businessType = BusinessType.INSERT)
    @PostMapping("/dataset")
    public R<ReportDataset> saveDataset(@RequestBody ReportDataset dataset) {
        return R.ok("保存成功", reportService.saveDataset(dataset));
    }

    @SaCheckPermission("report:page:list")
    @DeleteMapping("/dataset/{datasetIds}")
    public R<Void> removeDatasets(@PathVariable List<Long> datasetIds) {
        reportService.deleteDatasets(datasetIds);
        return R.ok("删除成功", null);
    }

    /** 执行数据集（带参数） */
    @SaCheckPermission("report:page:list")
    @PostMapping("/dataset/execute")
    public R<SqlResult> execute(@RequestBody Map<String, Object> body) {
        Long datasetId = Long.valueOf(String.valueOf(body.get("datasetId")));
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) body.getOrDefault("params", Map.of());
        return R.ok(reportService.executeDataset(datasetId, params));
    }

    // ---------- 图表 ----------
    @SaCheckPermission("report:page:list")
    @GetMapping("/chart/list")
    public R<List<ReportChart>> chartList(@RequestParam Long pageId) {
        return R.ok(reportService.chartList(pageId));
    }

    @SaCheckPermission("report:page:list")
    @Log(title = "报表图表", businessType = BusinessType.INSERT)
    @PostMapping("/chart")
    public R<ReportChart> saveChart(@RequestBody ReportChart chart) {
        return R.ok("保存成功", reportService.saveChart(chart));
    }

    @SaCheckPermission("report:page:list")
    @DeleteMapping("/chart/{chartIds}")
    public R<Void> removeCharts(@PathVariable List<Long> chartIds) {
        reportService.deleteCharts(chartIds);
        return R.ok("删除成功", null);
    }
}
