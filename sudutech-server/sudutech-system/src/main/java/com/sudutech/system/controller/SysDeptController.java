package com.sudutech.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.system.domain.SysDept;
import com.sudutech.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理
 */
@RestController
@RequestMapping("/api/v1/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @SaCheckPermission("system:dept:list")
    @GetMapping("/tree")
    public R<List<SysDept>> tree(SysDept query) {
        return R.ok(deptService.selectDeptTree(query));
    }

    @SaCheckPermission("system:dept:query")
    @GetMapping("/{deptId}")
    public R<SysDept> getInfo(@PathVariable Long deptId) {
        return R.ok(deptService.selectById(deptId));
    }

    @SaCheckPermission("system:dept:add")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysDept dept) {
        deptService.insertDept(dept);
        return R.ok("新增部门成功", null);
    }

    @SaCheckPermission("system:dept:edit")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysDept dept) {
        deptService.updateDept(dept);
        return R.ok("修改部门成功", null);
    }

    @SaCheckPermission("system:dept:remove")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public R<Void> remove(@PathVariable Long deptId) {
        deptService.deleteById(deptId);
        return R.ok("删除成功", null);
    }
}
