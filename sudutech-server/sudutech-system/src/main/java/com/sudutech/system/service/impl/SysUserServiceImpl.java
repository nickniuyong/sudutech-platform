package com.sudutech.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.constant.Constants;
import com.sudutech.common.core.domain.LoginUser;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.LoginHelper;
import com.sudutech.common.utils.SecurityUtils;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysDept;
import com.sudutech.system.domain.SysRole;
import com.sudutech.system.domain.SysUser;
import com.sudutech.system.domain.SysUserPost;
import com.sudutech.system.domain.SysUserRole;
import com.sudutech.system.domain.table.SysUserTableDef;
import com.sudutech.system.mapper.*;
import com.sudutech.system.service.SysConfigService;
import com.sudutech.system.service.SysDeptService;
import com.sudutech.system.service.SysRoleService;
import com.sudutech.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sudutech.system.domain.table.SysUserTableDef.SYS_USER;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleService roleService;
    private final SysDeptService deptService;
    private final SysDeptMapper deptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;
    private final SysPostMapper postMapper;
    private final SysConfigService configService;

    @Override
    public SysUser selectByUserName(String userName) {
        return userMapper.selectOneByCondition(
                SYS_USER.USER_NAME.eq(userName).and(SYS_USER.DEL_FLAG.eq(Constants.NOT_DELETED)));
    }

    @Override
    public Page<SysUser> pageList(SysUser query, PageQuery pageQuery) {
        QueryWrapper wrapper = QueryWrapper.create().where(SYS_USER.DEL_FLAG.eq(Constants.NOT_DELETED));
        if (query != null) {
            if (StringUtils.isNotBlank(query.getUserName())) {
                wrapper.and(SYS_USER.USER_NAME.like(query.getUserName()));
            }
            if (StringUtils.isNotBlank(query.getNickName())) {
                wrapper.and(SYS_USER.NICK_NAME.like(query.getNickName()));
            }
            if (StringUtils.isNotBlank(query.getStatus())) {
                wrapper.and(SYS_USER.STATUS.eq(query.getStatus()));
            }
            if (StringUtils.isNotBlank(query.getPhone())) {
                wrapper.and(SYS_USER.PHONE.like(query.getPhone()));
            }
            if (query.getDeptId() != null) {
                // 含子部门
                List<Long> deptIds = deptService.selectDeptAndChildrenIds(query.getDeptId());
                wrapper.and(SYS_USER.DEPT_ID.in(deptIds));
            }
        }
        // 数据权限：非管理员按角色 data_scope 过滤部门
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser != null && !loginUser.isAdmin()) {
            List<Long> scopeDeptIds = roleService.selectDataScopeDeptIds(loginUser);
            if (scopeDeptIds == null) {
                // 仅本人
                wrapper.and(SYS_USER.USER_ID.eq(loginUser.getUserId()));
            } else if (scopeDeptIds.isEmpty()) {
                return Page.of(pageQuery.getPageNum(), pageQuery.getPageSize());
            } else {
                wrapper.and(SYS_USER.DEPT_ID.in(scopeDeptIds));
            }
        }
        wrapper.orderBy(SYS_USER.USER_ID.asc());
        Page<SysUser> page = userMapper.paginate(pageQuery.toPage(), wrapper);
        page.getRecords().forEach(u -> {
            u.setPassword(null);
            if (u.getDeptId() != null) {
                SysDept dept = deptMapper.selectOneById(u.getDeptId());
                u.setDeptName(dept != null ? dept.getDeptName() : "");
            }
        });
        return page;
    }

    @Override
    public List<SysUser> selectList(SysUser query) {
        QueryWrapper wrapper = QueryWrapper.create().where(SYS_USER.DEL_FLAG.eq(Constants.NOT_DELETED))
                .and(SYS_USER.STATUS.eq(Constants.STATUS_NORMAL));
        if (query != null && query.getDeptId() != null) {
            wrapper.and(SYS_USER.DEPT_ID.eq(query.getDeptId()));
        }
        List<SysUser> users = userMapper.selectListByQuery(wrapper);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public SysUser selectById(Long userId) {
        SysUser user = userMapper.selectOneById(userId);
        if (user != null) {
            user.setPassword(null);
            SysDept dept = deptMapper.selectOneById(user.getDeptId());
            if (dept != null) {
                user.setDeptName(dept.getDeptName());
            }
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertUser(SysUser user) {
        checkUserNameUnique(user.getUserName(), null);
        if (StringUtils.isBlank(user.getPassword())) {
            String initPwd = configService.getConfigValue("sys.user.initPassword", "123456");
            user.setPassword(SecurityUtils.encodePassword(initPwd));
        } else {
            user.setPassword(SecurityUtils.encodePassword(user.getPassword()));
        }
        user.setUserId(null);
        boolean ok = userMapper.insert(user) > 0;
        if (ok) {
            insertUserRelations(user);
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        SysUser exist = userMapper.selectOneById(user.getUserId());
        if (exist == null) {
            throw new ServiceException("用户不存在");
        }
        if (exist.isAdmin()) {
            throw new ServiceException("不允许修改超级管理员");
        }
        checkUserNameUnique(user.getUserName(), user.getUserId());
        user.setPassword(null);
        user.setDelFlag(null);
        boolean ok = userMapper.update(user) > 0;
        if (ok) {
            userRoleMapper.deleteUserRoleByUserId(user.getUserId());
            postMapper.deleteUserPostByUserId(user.getUserId());
            insertUserRelations(user);
        }
        return ok;
    }

    private void insertUserRelations(SysUser user) {
        if (user.getRoleIds() != null) {
            for (Long roleId : user.getRoleIds()) {
                userRoleMapper.insert(new SysUserRole(user.getUserId(), roleId), false);
            }
        }
        if (user.getPostIds() != null) {
            for (Long postId : user.getPostIds()) {
                userPostMapper.insert(new SysUserPost(user.getUserId(), postId), false);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        if (userIds.contains(Constants.SUPER_ADMIN_ID)) {
            throw new ServiceException("超级管理员不允许删除");
        }
        return userMapper.deleteBatchByIds(userIds);
    }

    @Override
    public boolean resetPwd(Long userId, String newPassword) {
        SysUser exist = userMapper.selectOneById(userId);
        if (exist == null) {
            throw new ServiceException("用户不存在");
        }
        if (exist.isAdmin() && !LoginHelper.getLoginUser().isAdmin()) {
            throw new ServiceException("只有超级管理员可重置管理员密码");
        }
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setPassword(SecurityUtils.encodePassword(newPassword));
        return userMapper.update(update) > 0;
    }

    @Override
    public boolean changeStatus(Long userId, String status) {
        if (Constants.SUPER_ADMIN_ID.equals(userId)) {
            throw new ServiceException("不允许停用超级管理员");
        }
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setStatus(status);
        return userMapper.update(update) > 0;
    }

    @Override
    public boolean changePwd(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        if (!SecurityUtils.matchesPassword(oldPassword, user.getPassword())) {
            throw new ServiceException("原密码错误");
        }
        SecurityUtils.checkPasswordStrength(newPassword);
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setPassword(SecurityUtils.encodePassword(newPassword));
        return userMapper.update(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantRole(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteUserRoleByUserId(userId);
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                userRoleMapper.insert(new SysUserRole(userId, roleId), false);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantPost(Long userId, List<Long> postIds) {
        postMapper.deleteUserPostByUserId(userId);
        if (postIds != null) {
            for (Long postId : postIds) {
                userPostMapper.insert(new SysUserPost(userId, postId), false);
            }
        }
    }

    @Override
    public List<Long> selectRoleIdsByUserId(Long userId) {
        return roleMapper.selectRolesByUserId(userId).stream().map(SysRole::getRoleId).toList();
    }

    @Override
    public List<Long> selectPostIdsByUserId(Long userId) {
        return userPostMapper.selectListByCondition(
                com.mybatisflex.core.query.QueryCondition.create(
                        com.sudutech.system.domain.table.SysUserPostTableDef.SYS_USER_POST.USER_ID, userId))
                .stream().map(SysUserPost::getPostId).toList();
    }

    @Override
    public void updateLoginInfo(Long userId, String ip) {
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setLoginIp(ip);
        update.setLoginDate(java.time.LocalDateTime.now());
        userMapper.update(update);
    }

    @Override
    public List<SysUser> selectUsersByRoleKey(String roleKey) {
        List<SysUser> users = userMapper.selectUsersByRoleKey(roleKey);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public Map<String, Object> importUsers(List<SysUser> users) {
        int success = 0;
        List<String> errors = new ArrayList<>();
        for (SysUser user : users) {
            try {
                if (selectByUserName(user.getUserName()) != null) {
                    errors.add(user.getUserName() + " 已存在");
                    continue;
                }
                insertUser(user);
                success++;
            } catch (Exception e) {
                errors.add(user.getUserName() + ": " + e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("errors", errors);
        return result;
    }

    private void checkUserNameUnique(String userName, Long excludeUserId) {
        SysUser exist = selectByUserName(userName);
        if (exist != null && (excludeUserId == null || !exist.getUserId().equals(excludeUserId))) {
            throw new ServiceException("登录账号已存在: " + userName);
        }
    }
}
