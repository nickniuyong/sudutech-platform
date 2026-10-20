package com.sudutech.system.service;

import com.sudutech.system.domain.SysMenu;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 菜单服务
 */
public interface SysMenuService {

    /** 查询全部菜单树（管理界面） */
    List<SysMenu> selectMenuTree(SysMenu query);

    /** 按用户查询可见菜单并构建前端路由树 */
    List<Map<String, Object>> buildRoutersByUserId(Long userId);

    List<SysMenu> selectMenusByUserId(Long userId);

    List<SysMenu> selectAll();

    SysMenu selectById(Long menuId);

    boolean insertMenu(SysMenu menu);

    boolean updateMenu(SysMenu menu);

    int deleteById(Long menuId);

    /** 查询当前用户全部权限标识集合 */
    Set<String> selectPermsByUserId(Long userId);

    /** 构建前端路由节点 */
    Map<String, Object> toRouter(SysMenu menu);
}
