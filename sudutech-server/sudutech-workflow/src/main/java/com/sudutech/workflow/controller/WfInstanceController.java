package com.sudutech.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.core.domain.TableDataInfo;
import com.sudutech.workflow.domain.WfBusiness;
import com.sudutech.workflow.domain.WfOperationLog;
import com.sudutech.workflow.domain.WfTask;
import com.sudutech.workflow.service.WfInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程实例与任务
 */
@RestController
@RequestMapping("/api/v1/wf")
@RequiredArgsConstructor
public class WfInstanceController {

    private final WfInstanceService instanceService;

    /** 启动流程实例 */
    @SaCheckLogin
    @Log(title = "流程实例", businessType = BusinessType.INSERT)
    @PostMapping("/instance/start")
    public R<WfBusiness> start(@RequestBody Map<String, String> body) {
        WfBusiness business = instanceService.start(
                body.getOrDefault("processKey", ""),
                body.getOrDefault("businessId", ""),
                body.getOrDefault("businessTitle", ""),
                body.get("formData"));
        return R.ok("流程已发起", business);
    }

    @SaCheckLogin
    @GetMapping("/task/todo")
    public TableDataInfo<WfTask> todo(@RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        Page<WfTask> page = instanceService.todoTasks(pageNum, pageSize);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckLogin
    @GetMapping("/task/done")
    public TableDataInfo<WfTask> done(@RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        Page<WfTask> page = instanceService.doneTasks(pageNum, pageSize);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckLogin
    @GetMapping("/instance/my")
    public TableDataInfo<WfBusiness> my(@RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        Page<WfBusiness> page = instanceService.myInstances(pageNum, pageSize);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckLogin
    @Log(title = "流程实例", businessType = BusinessType.UPDATE)
    @PostMapping("/task/approve")
    public R<Void> approve(@RequestBody Map<String, String> body) {
        instanceService.approve(Long.valueOf(body.get("taskId")), body.get("comment"));
        return R.ok("审批通过", null);
    }

    @SaCheckLogin
    @Log(title = "流程实例", businessType = BusinessType.UPDATE)
    @PostMapping("/task/reject")
    public R<Void> reject(@RequestBody Map<String, String> body) {
        instanceService.reject(Long.valueOf(body.get("taskId")), body.get("comment"));
        return R.ok("已驳回", null);
    }

    @SaCheckLogin
    @Log(title = "流程实例", businessType = BusinessType.UPDATE)
    @PostMapping("/task/delegate")
    public R<Void> delegate(@RequestBody Map<String, Object> body) {
        instanceService.delegate(Long.valueOf(String.valueOf(body.get("taskId"))),
                Long.valueOf(String.valueOf(body.get("targetUserId"))),
                (String) body.get("comment"));
        return R.ok("委派成功", null);
    }

    /** 流程轨迹 + 任务明细 */
    @SaCheckLogin
    @GetMapping("/instance/{instanceId}/trace")
    public R<Map<String, Object>> trace(@PathVariable String instanceId) {
        List<WfOperationLog> logs = instanceService.trace(instanceId);
        List<WfTask> tasks = instanceService.instanceTasks(instanceId);
        Map<String, Object> data = new HashMap<>();
        data.put("logs", logs);
        data.put("tasks", tasks);
        return R.ok(data);
    }
}
