package com.sudutech.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.constant.Constants;
import com.sudutech.common.core.domain.LoginUser;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.LoginHelper;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysDept;
import com.sudutech.system.domain.SysRole;
import com.sudutech.system.domain.SysRoleDept;
import com.sudutech.system.domain.SysRoleMenu;
import com.sudutech.system.domain.table.SysRoleTableDef;
import com.sudutech.system.mapper.SysDeptMapper;
import com.sudutech.system.mapper.SysRoleDeptMapper;
import com.sudutech.system.mapper.SysRoleMapper;
import com.sudutech.system.mapper.SysRoleMenuMapper;
import com.sudutech.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.sudutech.system.domain.table.SysRoleTableDef.SYS_ROLE;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysDeptMapper deptMapper;

    @Override
    public Page<SysRole> pageList(SysRole query, PageQuery pageQuery) {
        QueryWrapper wrapper = QueryWrapper.create().where(SYS_ROLE.DEL_FLAG.eq(Constants.NOT_DELETED));
        if (query != null) {
            if (StringUtils.isNotBlank(query.getRoleName())) {
                wrapper.and(SYS_ROLE.ROLE_NAME.like(query.getRoleName()));
            }
            if (StringUtils.isNotBlank(query.getRoleKey())) {
                wrapper.and(SYS_ROLE.ROLE_KEY.like(query.getRoleKey()));
            }
            if (StringUtils.isNotBlank(query.getStatus())) {
                wrapper.and(SYS_ROLE.STATUS.eq(query.getStatus()));
            }
        }
        wrapper.orderBy(SYS_ROLE.ROLE_SORT.asc());
        return roleMapper.paginate(pageQuery.toPage(), wrapper);
    }

    @Override
    public List<SysRole> selectAll(String status) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SYS_ROLE.DEL_FLAG.eq(Constants.NOT_DELETED))
                .orderBy(SYS_ROLE.ROLE_SORT.asc());
        if (StringUtils.isNotBlank(status)) {
            wrapper.and(SYS_ROLE.STATUS.eq(status));
        }
        return roleMapper.selectListByQuery(wrapper);
    }

    @Override
    public SysRole selectById(Long roleId) {
        return roleMapper.selectOneById(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRole(SysRole role) {
        checkRoleUnique(role);
        role.setRoleId(null);
        boolean ok = roleMapper.insert(role) > 0;
        if (ok && role.getMenuIds() != null) {
            insertRoleMenus(role.getRoleId(), role.getMenuIds());
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role) {
        SysRole exist = roleMapper.selectOneById(role.getRoleId());
        if (exist == null) {
            throw new ServiceException("角色不存在");
        }
        if (exist.isAdmin()) {
            throw new ServiceException("内置角色不允许修改");
        }
        checkRoleUnique(role);
        role.setDelFlag(null);
        boolean ok = roleMapper.update(role) > 0;
        if (ok && role.getMenuIds() != null) {
            roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
            insertRoleMenus(role.getRoleId(), role.getMenuIds());
        }
        if (ok && role.getDeptIds() != null) {
            roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
            if (role.getDeptIds() != null) {
                for (Long deptId : role.getDeptIds()) {
                    roleDeptMapper.insert(new SysRoleDept(role.getRoleId(), deptId), false);
                }
            }
        }
        return ok;
    }

    private void insertRoleMenus(Long roleId, List<Long> menuIds) {
        for (Long menuId : menuIds) {
            roleMenuMapper.insert(new SysRoleMenu(roleId, menuId), false);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return 0;
        }
        if (roleIds.contains(1L)) {
            throw new ServiceException("内置角色不允许删除");
        }
        return roleMapper.deleteBatchByIds(roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantMenu(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        if (menuIds != null) {
            insertRoleMenus(roleId, menuIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantDept(Long roleId, List<Long> deptIds) {
        roleDeptMapper.deleteRoleDeptByRoleId(roleId);
        if (deptIds != null) {
            for (Long deptId : deptIds) {
                roleDeptMapper.insert(new SysRoleDept(roleId, deptId), false);
            }
        }
    }

    @Override
    public List<Long> selectMenuIdsByRole(Long roleId) {
        return roleMenuMapper.selectListByCondition(
                com.mybatisflex.core.query.QueryCondition.create(
                        com.sudutech.system.domain.table.SysRoleMenuTableDef.SYS_ROLE_MENU.ROLE_ID, roleId))
                .stream().map(SysRoleMenu::getMenuId).toList();
    }

    @Override
    public List<Long> selectDeptIdsByRole(Long roleId) {
        return roleDeptMapper.selectListByCondition(
                com.mybatisflex.core.query.QueryCondition.create(
                        com.sudutech.system.domain.table.SysRoleDeptTableDef.SYS_ROLE_DEPT.ROLE_ID, roleId))
                .stream().map(SysRoleDept::getDeptId).toList();
    }

    @Override
    public void changeStatus(Long roleId, String status) {
        if (roleId == 1L) {
            throw new ServiceException("内置角色不允许停用");
        }
        SysRole role = new SysRole();
        role.setRoleId(roleId);
        role.setStatus(status);
        roleMapper.update(role);
    }

    @Override
    public List<Long> selectDataScopeDeptIds(LoginUser loginUser) {
        if (loginUser.isAdmin()) {
            return null; // null 标记全部权限，但 admin 由调用方先判断
        }
        List<SysRole> roles = roleMapper.selectRolesByUserId(loginUser.getUserId());
        Set<String> scopes = new HashSet<>();
        roles.forEach(r -> scopes.add(r.getDataScope()));
        // 任一角色拥有全部权限
        if (scopes.contains("1") || loginUser.hasRole(Constants.SUPER_ADMIN_KEY)) {
            return new ArrayList<>();
        }
        Set<Long> deptIds = new HashSet<>();
        boolean selfOnly = scopes.contains("5") && scopes.size() == 1;
        if (scopes.contains("2")) {
            // 自定义
            for (SysRole role : roles) {
                if ("2".equals(role.getDataScope())) {
                    deptMapper.selectDeptsByRoleId(role.getRoleId()).forEach(d -> deptIds.add(d.getDeptId()));
                }
            }
        }
        if (scopes.contains("3") || scopes.contains("4")) {
            Long userDeptId = loginUser.getDeptId();
            if (userDeptId != null) {
                if (scopes.contains("3")) {
                    deptIds.add(userDeptId);
                }
                if (scopes.contains("4")) {
                    deptIds.add(userDeptId);
                    SysDept dept = deptMapper.selectOneById(userDeptId);
                    if (dept != null && StringUtils.isNotBlank(dept.getAncestors())) {
                        // 祖级列表后缀匹配子部门
                        String prefix = dept.getAncestors() + "," + userDeptId;
                        com.mybatisflex.core.query.QueryWrapper qw = com.mybatisflex.core.query.QueryWrapper.create()
                                .where(com.sudutech.system.domain.table.SysDeptTableDef.SYS_DEPT.ANCESTORS.likeLeft(prefix));
                        deptMapper.selectListByQuery(qw).forEach(d -> deptIds.add(d.getDeptId()));
                    }
                }
            }
        }
        if (selfOnly) {
            return null; // 仅本人
        }
        return new ArrayList<>(deptIds);
    }

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    private void checkRoleUnique(SysRole role) {
        if (StringUtils.isBlank(role.getRoleKey())) {
            throw new ServiceException("权限字符不能为空");
        }
        SysRole exist = roleMapper.selectOneByCondition(SYS_ROLE.ROLE_KEY.eq(role.getRoleKey())
                .and(SYS_ROLE.DEL_FLAG.eq(Constants.NOT_DELETED)));
        if (exist != null && (role.getRoleId() == null || !exist.getRoleId().equals(role.getRoleId()))) {
            throw new ServiceException("权限字符已存在: " + role.getRoleKey());
        }
    }
}
