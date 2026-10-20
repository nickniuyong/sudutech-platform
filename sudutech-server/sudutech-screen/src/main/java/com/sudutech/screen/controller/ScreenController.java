package com.sudutech.screen.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.sql.SqlResult;
import com.sudutech.screen.domain.ScreenComponent;
import com.sudutech.screen.domain.ScreenDataset;
import com.sudutech.screen.domain.ScreenPage;
import com.sudutech.screen.domain.ScreenProject;
import com.sudutech.screen.service.ScreenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 大屏管理
 */
@RestController
@RequestMapping("/api/v1/screen")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;

    // ---------- 项目 ----------
    @SaCheckPermission("screen:project:list")
    @GetMapping("/project/list")
    public R<List<ScreenProject>> projectList(@RequestParam(required = false) String projectName,
                                              @RequestParam(required = false) String status) {
        return R.ok(screenService.projectList(projectName, status));
    }

    @SaCheckPermission("screen:project:list")
    @GetMapping("/project/{projectId}")
    public R<ScreenProject> projectInfo(@PathVariable Long projectId) {
        return R.ok(screenService.selectProject(projectId));
    }

    @SaCheckPermission("screen:project:list")
    @Log(title = "大屏项目", businessType = BusinessType.INSERT)
    @PostMapping("/project")
    public R<ScreenProject> addProject(@RequestBody ScreenProject project) {
        return R.ok("新增成功", screenService.insertProject(project));
    }

    @SaCheckPermission("screen:project:list")
    @Log(title = "大屏项目", businessType = BusinessType.UPDATE)
    @PutMapping("/project")
    public R<ScreenProject> editProject(@RequestBody ScreenProject project) {
        return R.ok("修改成功", screenService.updateProject(project));
    }

    @SaCheckPermission("screen:project:list")
    @Log(title = "大屏项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/project/{projectIds}")
    public R<Void> removeProject(@PathVariable List<Long> projectIds) {
        screenService.deleteProjects(projectIds);
        return R.ok("删除成功", null);
    }

    /** 发布为只读分享页 */
    @SaCheckPermission("screen:project:list")
    @Log(title = "大屏发布", businessType = BusinessType.UPDATE)
    @PostMapping("/publish/{projectId}")
    public R<ScreenProject> publish(@PathVariable Long projectId) {
        return R.ok("发布成功", screenService.publish(projectId));
    }

    // ---------- 画布 ----------
    @SaCheckPermission("screen:project:list")
    @GetMapping("/page/{projectId}")
    public R<ScreenPage> getPage(@PathVariable Long projectId) {
        return R.ok(screenService.selectPage(projectId));
    }

    @SaCheckPermission("screen:project:list")
    @Log(title = "大屏画布", businessType = BusinessType.UPDATE)
    @PutMapping("/page")
    public R<ScreenPage> savePage(@RequestBody ScreenPage page) {
        return R.ok("保存成功", screenService.savePage(page));
    }

    /** 组件库 */
    @SaCheckLogin
    @GetMapping("/component/list")
    public R<List<ScreenComponent>> components() {
        return R.ok(screenService.componentList());
    }

    // ---------- 数据集 ----------
    @SaCheckPermission("screen:project:list")
    @GetMapping("/dataset/list")
    public R<List<ScreenDataset>> datasetList(@RequestParam(required = false) Long projectId) {
        return R.ok(screenService.datasetList(projectId));
    }

    @SaCheckPermission("screen:project:list")
    @Log(title = "大屏数据集", businessType = BusinessType.INSERT)
    @PostMapping("/dataset")
    public R<ScreenDataset> saveDataset(@RequestBody ScreenDataset dataset) {
        return R.ok("保存成功", screenService.saveDataset(dataset));
    }

    @SaCheckPermission("screen:project:list")
    @DeleteMapping("/dataset/{datasetIds}")
    public R<Void> removeDatasets(@PathVariable List<Long> datasetIds) {
        screenService.deleteDatasets(datasetIds);
        return R.ok("删除成功", null);
    }

    /** 数据集预览执行（带参数） */
    @SaCheckPermission("screen:project:list")
    @PostMapping("/dataset/preview")
    public R<SqlResult> preview(@RequestBody Map<String, Object> body) {
        Long datasetId = Long.valueOf(String.valueOf(body.get("datasetId")));
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) body.getOrDefault("params", Map.of());
        int limit = body.get("limit") == null ? 200 : Integer.parseInt(String.valueOf(body.get("limit")));
        return R.ok(screenService.executeDataset(datasetId, params, limit));
    }
}
