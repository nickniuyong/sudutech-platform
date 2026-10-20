package com.sudutech.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.core.domain.TableDataInfo;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysMenu;
import com.sudutech.system.domain.SysRole;
import com.sudutech.system.service.SysMenuService;
import com.sudutech.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理
 */
@RestController
@RequestMapping("/api/v1/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;
    private final SysMenuService menuService;

    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public TableDataInfo<SysRole> list(SysRole query, PageQuery pageQuery) {
        Page<SysRole> page = roleService.pageList(query, pageQuery);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckPermission("system:role:query")
    @GetMapping("/{roleId}")
    public R<Map<String, Object>> getInfo(@PathVariable Long roleId) {
        Map<String, Object> data = new HashMap<>();
        data.put("role", roleId > 0 ? roleService.selectById(roleId) : new SysRole());
        data.put("menuIds", roleId > 0 ? roleService.selectMenuIdsByRole(roleId) : List.of());
        data.put("deptIds", roleId > 0 ? roleService.selectDeptIdsByRole(roleId) : List.of());
        return R.ok(data);
    }

    @GetMapping("/all")
    public R<List<SysRole>> all() {
        return R.ok(roleService.selectAll("0"));
    }

    @SaCheckPermission("system:role:add")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        roleService.insertRole(role);
        return R.ok("新增角色成功", null);
    }

    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysRole role) {
        roleService.updateRole(role);
        return R.ok("修改角色成功", null);
    }

    @SaCheckPermission("system:role:remove")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public R<Void> remove(@PathVariable String roleIds) {
        List<Long> ids = Arrays.stream(roleIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
        roleService.deleteByIds(ids);
        return R.ok("删除成功", null);
    }

    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/grantMenu")
    public R<Void> grantMenu(@RequestBody Map<String, Object> body) {
        Long roleId = Long.valueOf(String.valueOf(body.get("roleId")));
        @SuppressWarnings("unchecked")
        List<Number> menuIds = (List<Number>) body.getOrDefault("menuIds", List.of());
        roleService.grantMenu(roleId, menuIds.stream().map(Number::longValue).collect(Collectors.toList()));
        return R.ok("菜单授权成功", null);
    }

    @SaCheckPermission("system:role:edit")
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody Map<String, Object> body) {
        roleService.changeStatus(Long.valueOf(String.valueOf(body.get("roleId"))),
                String.valueOf(body.get("status")));
        return R.ok("状态修改成功", null);
    }

    /** 菜单树下拉（角色授权用） */
    @GetMapping("/menuTree")
    public R<List<SysMenu>> menuTree() {
        return R.ok(menuService.selectMenuTree(null));
    }
}
