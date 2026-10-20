package com.sudutech.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.workflow.domain.WfDefinitionExt;
import com.sudutech.workflow.domain.WfForm;
import com.sudutech.workflow.domain.WfNodeConfig;
import com.sudutech.workflow.service.WfDefinitionService;
import com.sudutech.workflow.service.WfFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程定义与表单
 */
@RestController
@RequestMapping("/api/v1/wf")
@RequiredArgsConstructor
public class WfDefinitionController {

    private final WfDefinitionService definitionService;
    private final WfFormService formService;

    @SaCheckPermission("wf:def:list")
    @GetMapping("/definition/list")
    public R<List<WfDefinitionExt>> list(@RequestParam(required = false) String processName,
                                         @RequestParam(required = false) String status) {
        return R.ok(definitionService.selectList(processName, status));
    }

    @SaCheckPermission("wf:def:list")
    @GetMapping("/definition/{id}")
    public R<WfDefinitionExt> getInfo(@PathVariable Long id) {
        return R.ok(definitionService.selectById(id));
    }

    /** 上传 BPMN XML 并部署 */
    @SaCheckPermission("wf:def:list")
    @Log(title = "流程定义", businessType = BusinessType.IMPORT)
    @PostMapping("/definition/deploy")
    public R<WfDefinitionExt> deploy(@RequestBody WfDefinitionExt definition) {
        return R.ok("部署成功", definitionService.deploy(definition));
    }

    /** 获取 BPMN XML（设计器回显） */
    @SaCheckPermission("wf:def:list")
    @GetMapping("/definition/{id}/xml")
    public R<String> getXml(@PathVariable Long id) {
        return R.ok(definitionService.selectXml(id));
    }

    @SaCheckPermission("wf:def:list")
    @Log(title = "流程定义", businessType = BusinessType.DELETE)
    @DeleteMapping("/definition/{id}")
    public R<Void> delete(@PathVariable Long id) {
        definitionService.deleteById(id);
        return R.ok("删除成功", null);
    }

    @SaCheckPermission("wf:def:list")
    @PutMapping("/definition/status")
    public R<Void> changeStatus(@RequestBody Map<String, Object> body) {
        definitionService.updateStatus(Long.valueOf(String.valueOf(body.get("id"))),
                String.valueOf(body.get("status")));
        return R.ok("操作成功", null);
    }

    /** 节点配置读取/保存 */
    @SaCheckPermission("wf:def:list")
    @GetMapping("/definition/{id}/nodes")
    public R<List<WfNodeConfig>> nodes(@PathVariable Long id) {
        return R.ok(definitionService.selectNodeConfigs(id));
    }

    @SaCheckPermission("wf:def:list")
    @Log(title = "流程节点配置", businessType = BusinessType.UPDATE)
    @PostMapping("/definition/{id}/nodes")
    public R<Void> saveNodes(@PathVariable Long id, @RequestBody List<WfNodeConfig> configs) {
        definitionService.saveNodeConfigs(id, configs);
        return R.ok("节点配置已保存", null);
    }

    // ---------------- 动态表单 ----------------

    @SaCheckPermission("wf:form:list")
    @GetMapping("/form/list")
    public R<List<WfForm>> formList(@RequestParam(required = false) String formName) {
        return R.ok(formService.selectList(formName));
    }

    @SaCheckPermission("wf:form:list")
    @GetMapping("/form/{id}")
    public R<WfForm> formInfo(@PathVariable Long id) {
        return R.ok(formService.selectById(id));
    }

    @SaCheckPermission("wf:form:list")
    @Log(title = "流程表单", businessType = BusinessType.INSERT)
    @PostMapping("/form")
    public R<Void> addForm(@RequestBody WfForm form) {
        formService.save(form);
        return R.ok("保存成功", null);
    }

    @SaCheckPermission("wf:form:list")
    @Log(title = "流程表单", businessType = BusinessType.UPDATE)
    @PutMapping("/form")
    public R<Void> editForm(@RequestBody WfForm form) {
        formService.save(form);
        return R.ok("保存成功", null);
    }

    @SaCheckPermission("wf:form:list")
    @Log(title = "流程表单", businessType = BusinessType.DELETE)
    @DeleteMapping("/form/{id}")
    public R<Void> removeForm(@PathVariable Long id) {
        formService.deleteById(id);
        return R.ok("删除成功", null);
    }
}
