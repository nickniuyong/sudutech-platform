package com.sudutech.system.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.constant.Constants;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysMenu;
import com.sudutech.system.domain.SysUser;
import com.sudutech.system.domain.table.SysMenuTableDef;
import com.sudutech.system.mapper.SysMenuMapper;
import com.sudutech.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sudutech.system.domain.table.SysMenuTableDef.SYS_MENU;

/**
 * 菜单服务实现
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper menuMapper;

    @Override
    public List<SysMenu> selectMenuTree(SysMenu query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SYS_MENU.STATUS.eq(Constants.STATUS_NORMAL))
                .orderBy(SYS_MENU.PARENT_ID.asc(), SYS_MENU.ORDER_NUM.asc());
        if (query != null && StringUtils.isNotBlank(query.getMenuName())) {
            // 模糊搜索时返回平铺列表
            return menuMapper.selectListByCondition(
                    SYS_MENU.MENU_NAME.like(query.getMenuName()).and(SYS_MENU.STATUS.eq(Constants.STATUS_NORMAL)));
        }
        return buildTree(menuMapper.selectListByQuery(wrapper), 0L);
    }

    @Override
    public List<SysMenu> selectMenusByUserId(Long userId) {
        return menuMapper.selectMenusByUserId(userId);
    }

    @Override
    public List<SysMenu> selectAll() {
        return menuMapper.selectAll().stream()
                .sorted(Comparator.comparing(SysMenu::getParentId).thenComparing(SysMenu::getOrderNum))
                .toList();
    }

    @Override
    public List<Map<String, Object>> buildRoutersByUserId(Long userId) {
        List<SysMenu> menus;
        if (userId != null && userId == 1L) {
            menus = selectAll().stream()
                    .filter(m -> !"F".equals(m.getMenuType()))
                    .filter(m -> "0".equals(m.getStatus()))
                    .toList();
        } else {
            menus = menuMapper.selectMenusByUserId(userId).stream()
                    .filter(m -> !"F".equals(m.getMenuType()))
                    .toList();
        }
        List<SysMenu> tree = buildTree(new ArrayList<>(menus), 0L);
        return tree.stream().map(this::toRouter).toList();
    }

    @Override
    public SysMenu selectById(Long menuId) {
        return menuMapper.selectOneById(menuId);
    }

    @Override
    public boolean insertMenu(SysMenu menu) {
        validate(menu);
        menu.setMenuId(null);
        return menuMapper.insert(menu) > 0;
    }

    @Override
    public boolean updateMenu(SysMenu menu) {
        validate(menu);
        if (menu.getMenuId().equals(menu.getParentId())) {
            throw new ServiceException("上级菜单不能选择自己");
        }
        return menuMapper.update(menu) > 0;
    }

    @Override
    public int deleteById(Long menuId) {
        long childCount = menuMapper.selectCountByCondition(SYS_MENU.PARENT_ID.eq(menuId));
        if (childCount > 0) {
            throw new ServiceException("存在子菜单，不允许删除");
        }
        return menuMapper.deleteById(menuId);
    }

    @Override
    public Set<String> selectPermsByUserId(Long userId) {
        Set<String> perms = new HashSet<>();
        if (userId != null && userId == 1L) {
            perms.add(Constants.ALL_PERMISSION);
            return perms;
        }
        perms.addAll(menuMapper.selectPermsByUserId(userId));
        return perms;
    }

    private void validate(SysMenu menu) {
        if (StringUtils.isBlank(menu.getMenuName())) {
            throw new ServiceException("菜单名称不能为空");
        }
        if (StringUtils.isBlank(menu.getMenuType())) {
            throw new ServiceException("菜单类型不能为空");
        }
    }

    private List<SysMenu> buildTree(List<SysMenu> menus, Long root) {
        Map<Long, List<SysMenu>> byParent = new HashMap<>();
        menus.forEach(m -> byParent.computeIfAbsent(m.getParentId(), k -> new ArrayList<>()).add(m));
        return buildChildren(root, byParent);
    }

    private List<SysMenu> buildChildren(Long parentId, Map<Long, List<SysMenu>> byParent) {
        List<SysMenu> children = byParent.getOrDefault(parentId, new ArrayList<>());
        children.sort(Comparator.comparing(m -> m.getOrderNum() == null ? 0 : m.getOrderNum()));
        children.forEach(m -> m.setChildren(buildChildren(m.getMenuId(), byParent)));
        return children;
    }

    @Override
    public Map<String, Object> toRouter(SysMenu menu) {
        Map<String, Object> router = new LinkedHashMap<>();
        // 路由 name：路径驼峰
        String name = StringUtils.toCamelCase(menu.getPath() == null ? "" : menu.getPath()
                .replace("/", ""));
        router.put("name", StringUtils.capitalize(name));
        router.put("path", menu.getPath());
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", menu.getMenuName());
        meta.put("icon", menu.getIcon());
        router.put("meta", meta);
        if ("M".equals(menu.getMenuType())) {
            router.put("component", "Layout");
            List<Map<String, Object>> childRouters = new ArrayList<>();
            if (menu.getChildren() != null) {
                menu.getChildren().stream()
                        .filter(c -> !"F".equals(c.getMenuType()))
                        .forEach(c -> childRouters.add(toRouter(c)));
            }
            router.put("children", childRouters);
        } else {
            router.put("component", menu.getComponent());
        }
        if ("1".equals(menu.getVisible())) {
            ((Map<String, Object>) router.get("meta")).put("hidden", true);
        }
        return router;
    }
}
