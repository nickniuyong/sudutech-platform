package com.sudutech.system.service;

import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysUser;

import java.util.List;
import java.util.Map;

/**
 * 用户服务
 */
public interface SysUserService {

    /** 按用户名查询（含密码，用于登录） */
    SysUser selectByUserName(String userName);

    Page<SysUser> pageList(SysUser query, PageQuery pageQuery);

    List<SysUser> selectList(SysUser query);

    SysUser selectById(Long userId);

    boolean insertUser(SysUser user);

    boolean updateUser(SysUser user);

    int deleteByIds(List<Long> userIds);

    boolean resetPwd(Long userId, String newPassword);

    boolean changeStatus(Long userId, String status);

    boolean changePwd(Long userId, String oldPassword, String newPassword);

    /** 分配角色 */
    void grantRole(Long userId, List<Long> roleIds);

    /** 分配岗位 */
    void grantPost(Long userId, List<Long> postIds);

    List<Long> selectRoleIdsByUserId(Long userId);

    List<Long> selectPostIdsByUserId(Long userId);

    /** 更新登录信息 */
    void updateLoginInfo(Long userId, String ip);

    /** 根据角色key查用户（工作流审批人解析用） */
    List<SysUser> selectUsersByRoleKey(String roleKey);

    Map<String, Object> importUsers(List<SysUser> users);
}
