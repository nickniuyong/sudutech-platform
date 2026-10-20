package com.sudutech.system.service;

import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.core.domain.LoginUser;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysRole;

import java.util.List;

/**
 * 角色服务
 */
public interface SysRoleService {

    Page<SysRole> pageList(SysRole query, PageQuery pageQuery);

    List<SysRole> selectAll(String status);

    SysRole selectById(Long roleId);

    boolean insertRole(SysRole role);

    boolean updateRole(SysRole role);

    int deleteByIds(List<Long> roleIds);

    void grantMenu(Long roleId, List<Long> menuIds);

    void grantDept(Long roleId, List<Long> deptIds);

    List<Long> selectMenuIdsByRole(Long roleId);

    List<Long> selectDeptIdsByRole(Long roleId);

    void changeStatus(Long roleId, String status);

    /** 计算当前用户的数据权限部门ID列表；null 表示仅本人；空集合表示无权限 */
    List<Long> selectDataScopeDeptIds(LoginUser loginUser);

    List<SysRole> selectRolesByUserId(Long userId);
}
