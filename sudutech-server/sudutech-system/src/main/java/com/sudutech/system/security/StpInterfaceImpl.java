package com.sudutech.system.security;

import cn.dev33.satoken.stp.StpInterface;
import com.sudutech.common.core.domain.LoginUser;
import com.sudutech.common.utils.LoginHelper;
import com.sudutech.system.service.SysMenuService;
import com.sudutech.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Sa-Token 权限数据源：接口鉴权 @SaCheckPermission 的权限与角色来源
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysMenuService menuService;
    private final SysRoleService roleService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(String.valueOf(loginId));
        Set<String> perms = menuService.selectPermsByUserId(userId);
        return new ArrayList<>(perms);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(String.valueOf(loginId));
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser != null && loginUser.isAdmin()) {
            return List.of("admin");
        }
        return roleService.selectRolesByUserId(userId).stream()
                .map(r -> r.getRoleKey())
                .toList();
    }
}
