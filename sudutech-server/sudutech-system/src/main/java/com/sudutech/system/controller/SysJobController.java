package com.sudutech.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.core.domain.TableDataInfo;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysJob;
import com.sudutech.system.domain.SysJobLog;
import com.sudutech.system.service.SysJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定时任务
 */
@RestController
@RequestMapping("/api/v1/monitor/job")
@RequiredArgsConstructor
public class SysJobController {

    private final SysJobService jobService;

    @SaCheckPermission("monitor:job:list")
    @GetMapping("/list")
    public TableDataInfo<SysJob> list(SysJob query, PageQuery pageQuery) {
        Page<SysJob> page = jobService.pageList(query, pageQuery);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckPermission("monitor:job:query")
    @GetMapping("/{jobId}")
    public R<SysJob> getInfo(@PathVariable Long jobId) {
        return R.ok(jobService.selectById(jobId));
    }

    @SaCheckPermission("monitor:job:add")
    @Log(title = "定时任务", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysJob job) {
        jobService.insert(job);
        return R.ok("新增任务成功", null);
    }

    @SaCheckPermission("monitor:job:edit")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysJob job) {
        jobService.update(job);
        return R.ok("修改任务成功", null);
    }

    @SaCheckPermission("monitor:job:remove")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{jobIds}")
    public R<Void> remove(@PathVariable String jobIds) {
        List<Long> ids = Arrays.stream(jobIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
        jobService.deleteByIds(ids);
        return R.ok("删除成功", null);
    }

    @SaCheckPermission("monitor:job:run")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/run/{jobId}")
    public R<Void> run(@PathVariable Long jobId) {
        jobService.run(jobId);
        return R.ok("触发执行成功", null);
    }

    @SaCheckPermission("monitor:job:edit")
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody Map<String, Object> body) {
        jobService.changeStatus(Long.valueOf(String.valueOf(body.get("jobId"))),
                String.valueOf(body.get("status")));
        return R.ok("状态修改成功", null);
    }

    @SaCheckPermission("monitor:job:list")
    @GetMapping("/joblog/list")
    public TableDataInfo<SysJobLog> jobLogList(SysJobLog query, PageQuery pageQuery) {
        Page<SysJobLog> page = jobService.pageJobLog(query, pageQuery);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }
}
