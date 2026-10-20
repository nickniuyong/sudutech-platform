package com.sudutech.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.core.domain.TableDataInfo;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysDept;
import com.sudutech.system.domain.SysPost;
import com.sudutech.system.domain.SysRole;
import com.sudutech.system.domain.SysUser;
import com.sudutech.system.service.SysDeptService;
import com.sudutech.system.service.SysPostService;
import com.sudutech.system.service.SysRoleService;
import com.sudutech.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理
 */
@RestController
@RequestMapping("/api/v1/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysPostService postService;
    private final SysDeptService deptService;

    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public TableDataInfo<SysUser> list(SysUser query, PageQuery pageQuery) {
        Page<SysUser> page = userService.pageList(query, pageQuery);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckPermission("system:user:query")
    @GetMapping("/{userId}")
    public R<Map<String, Object>> getInfo(@PathVariable Long userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("user", userId > 0 ? userService.selectById(userId) : new SysUser());
        data.put("roleIds", userId > 0 ? userService.selectRoleIdsByUserId(userId) : List.of());
        data.put("postIds", userId > 0 ? userService.selectPostIdsByUserId(userId) : List.of());
        return R.ok(data);
    }

    /** 新增用户页初始化数据：部门树 + 岗位 + 角色 */
    @GetMapping("/init")
    public R<Map<String, Object>> init() {
        Map<String, Object> data = new HashMap<>();
        data.put("depts", deptService.selectDeptTree(null));
        data.put("posts", postService.selectAll());
        data.put("roles", roleService.selectAll("0"));
        return R.ok(data);
    }

    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysUser user) {
        userService.insertUser(user);
        return R.ok("新增用户成功", null);
    }

    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysUser user) {
        userService.updateUser(user);
        return R.ok("修改用户成功", null);
    }

    @SaCheckPermission("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable String userIds) {
        List<Long> ids = Arrays.stream(userIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
        userService.deleteByIds(ids);
        return R.ok("删除成功", null);
    }

    @SaCheckPermission("system:user:resetPwd")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(String.valueOf(body.get("userId")));
        String password = String.valueOf(body.get("password"));
        userService.resetPwd(userId, password);
        return R.ok("重置密码成功", null);
    }

    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(String.valueOf(body.get("userId")));
        String status = String.valueOf(body.get("status"));
        userService.changeStatus(userId, status);
        return R.ok("状态修改成功", null);
    }
}
