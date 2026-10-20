package com.sudutech.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.system.domain.SysMenu;
import com.sudutech.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理
 */
@RestController
@RequestMapping("/api/v1/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @SaCheckPermission("system:menu:list")
    @GetMapping("/tree")
    public R<List<SysMenu>> tree(SysMenu query) {
        return R.ok(menuService.selectMenuTree(query));
    }

    @SaCheckPermission("system:menu:query")
    @GetMapping("/{menuId}")
    public R<SysMenu> getInfo(@PathVariable Long menuId) {
        return R.ok(menuService.selectById(menuId));
    }

    @SaCheckPermission("system:menu:add")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysMenu menu) {
        menuService.insertMenu(menu);
        return R.ok("新增菜单成功", null);
    }

    @SaCheckPermission("system:menu:edit")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysMenu menu) {
        menuService.updateMenu(menu);
        return R.ok("修改菜单成功", null);
    }

    @SaCheckPermission("system:menu:remove")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public R<Void> remove(@PathVariable Long menuId) {
        menuService.deleteById(menuId);
        return R.ok("删除成功", null);
    }
}
