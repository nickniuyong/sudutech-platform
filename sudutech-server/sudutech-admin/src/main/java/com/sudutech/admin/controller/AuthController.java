package com.sudutech.admin.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.constant.Constants;
import com.sudutech.common.core.domain.LoginUser;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.utils.LoginHelper;
import com.sudutech.common.utils.ServletUtils;
import com.sudutech.system.domain.SysDept;
import com.sudutech.system.domain.SysRole;
import com.sudutech.system.domain.SysUser;
import com.sudutech.system.mapper.SysDeptMapper;
import com.sudutech.system.mapper.SysUserMapper;
import com.sudutech.system.service.SysLoginLogService;
import com.sudutech.system.service.SysMenuService;
import com.sudutech.system.service.SysRoleService;
import com.sudutech.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 认证：登录 / 登出 / 当前用户信息
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final SysDeptMapper deptMapper;
    private final SysLoginLogService loginLogService;

    @Log(title = "登录", businessType = BusinessType.OTHER, saveParam = false)
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String userName = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "");
        if (userName.isEmpty() || password.isEmpty()) {
            return R.fail("账号和密码不能为空");
        }
        SysUser user = userService.selectByUserName(userName);
        if (user == null) {
            loginLogService.recordLogin(userName, "1", "账号不存在");
            return R.fail("账号或密码错误");
        }
        if (Constants.STATUS_DISABLED.equals(user.getStatus())) {
            loginLogService.recordLogin(userName, "1", "账号已停用");
            return R.fail("账号已停用，请联系管理员");
        }
        if (!com.sudutech.common.utils.SecurityUtils.matchesPassword(password, user.getPassword())) {
            loginLogService.recordLogin(userName, "1", "密码错误");
            return R.fail("账号或密码错误");
        }

        // 构建 LoginUser 并登录（写入 Redis 会话）
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUserName(user.getUserName());
        loginUser.setNickName(user.getNickName());
        loginUser.setDeptId(user.getDeptId());
        SysDept dept = user.getDeptId() == null ? null : deptMapper.selectOneById(user.getDeptId());
        loginUser.setDeptName(dept != null ? dept.getDeptName() : "");
        List<SysRole> roles = roleService.selectRolesByUserId(user.getUserId());
        loginUser.setRoleKeys(roles.stream().map(SysRole::getRoleKey).collect(java.util.stream.Collectors.toSet()));
        String dataScope = "5";
        for (SysRole role : roles) {
            if ("1".equals(role.getDataScope())) {
                dataScope = "1";
                break;
            }
            dataScope = role.getDataScope();
        }
        loginUser.setDataScope(dataScope);
        LoginHelper.login(loginUser);
        userService.updateLoginInfo(user.getUserId(), ServletUtils.getClientIp(ServletUtils.getRequest()));
        loginLogService.recordLogin(userName, "0", "登录成功");

        Map<String, Object> data = new HashMap<>();
        data.put("token", com.sudutech.common.constant.Constants.TOKEN_PREFIX + cn.dev33.satoken.stp.StpUtil.getTokenValue());
        return R.ok("登录成功", data);
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        LoginUser user = LoginHelper.getLoginUser();
        if (user != null) {
            loginLogService.recordLogin(user.getUserName(), "0", "退出登录");
        }
        LoginHelper.logout();
        return R.ok("退出成功", null);
    }

    @SaCheckLogin
    @GetMapping("/userinfo")
    public R<Map<String, Object>> userinfo() {
        LoginUser loginUser = LoginHelper.getLoginUserRequired();
        Map<String, Object> data = new HashMap<>();
        data.put("user", userService.selectById(loginUser.getUserId()));
        data.put("roles", loginUser.isAdmin() ? List.of("admin")
                : new java.util.ArrayList<>(loginUser.getRoleKeys()));
        Set<String> perms = menuService.selectPermsByUserId(loginUser.getUserId());
        data.put("permissions", perms);
        data.put("routers", menuService.buildRoutersByUserId(loginUser.getUserId()));
        return R.ok(data);
    }
}
