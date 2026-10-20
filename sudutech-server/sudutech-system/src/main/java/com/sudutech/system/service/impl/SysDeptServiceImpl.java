package com.sudutech.system.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.constant.Constants;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysDept;
import com.sudutech.system.domain.SysUser;
import com.sudutech.system.domain.table.SysDeptTableDef;
import com.sudutech.system.mapper.SysDeptMapper;
import com.sudutech.system.mapper.SysUserMapper;
import com.sudutech.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sudutech.system.domain.table.SysDeptTableDef.SYS_DEPT;

/**
 * 部门服务实现
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl implements SysDeptService {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    @Override
    public List<SysDept> selectDeptTree(SysDept query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SYS_DEPT.DEL_FLAG.eq(Constants.NOT_DELETED))
                .orderBy(SYS_DEPT.PARENT_ID.asc(), SYS_DEPT.ORDER_NUM.asc());
        if (query != null && StringUtils.isNotBlank(query.getDeptName())) {
            return deptMapper.selectListByCondition(
                    SYS_DEPT.DEPT_NAME.like(query.getDeptName()).and(SYS_DEPT.DEL_FLAG.eq(Constants.NOT_DELETED)));
        }
        return buildTree(deptMapper.selectListByQuery(wrapper), 0L);
    }

    @Override
    public List<SysDept> selectAll() {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SYS_DEPT.DEL_FLAG.eq(Constants.NOT_DELETED))
                .orderBy(SYS_DEPT.ORDER_NUM.asc());
        return deptMapper.selectListByQuery(wrapper);
    }

    @Override
    public SysDept selectById(Long deptId) {
        return deptMapper.selectOneById(deptId);
    }

    @Override
    public boolean insertDept(SysDept dept) {
        SysDept parent = dept.getParentId() == null ? null : deptMapper.selectOneById(dept.getParentId());
        if (dept.getParentId() != null && parent == null) {
            throw new ServiceException("上级部门不存在");
        }
        dept.setDeptId(null);
        if (parent != null) {
            dept.setAncestors(parent.getAncestors() + "," + parent.getDeptId());
        } else {
            dept.setAncestors("0");
        }
        return deptMapper.insert(dept) > 0;
    }

    @Override
    public boolean updateDept(SysDept dept) {
        SysDept exist = deptMapper.selectOneById(dept.getDeptId());
        if (exist == null) {
            throw new ServiceException("部门不存在");
        }
        if (dept.getDeptId().equals(dept.getParentId())) {
            throw new ServiceException("上级部门不能选择自己");
        }
        String newAncestors;
        if (dept.getParentId() != null) {
            SysDept parent = deptMapper.selectOneById(dept.getParentId());
            if (parent == null) {
                throw new ServiceException("上级部门不存在");
            }
            if (parent.getAncestors() != null && parent.getAncestors().contains("," + dept.getDeptId() + ",")) {
                throw new ServiceException("不允许将部门移动到自己的子部门下");
            }
            newAncestors = (parent.getParentId() == 0 ? "0" : parent.getAncestors() + "," + parent.getDeptId());
        } else {
            newAncestors = exist.getAncestors();
        }
        dept.setAncestors(newAncestors);
        boolean ok = deptMapper.update(dept) > 0;
        if (ok && StringUtils.isNotBlank(newAncestors) && !newAncestors.equals(exist.getAncestors())) {
            // 同步子部门祖级列表
            List<SysDept> children = deptMapper.selectListByCondition(
                    SYS_DEPT.ANCESTORS.like(exist.getAncestors() + "," + dept.getDeptId()));
            for (SysDept child : children) {
                SysDept upd = new SysDept();
                upd.setDeptId(child.getDeptId());
                upd.setAncestors(child.getAncestors()
                        .replace(exist.getAncestors() + "," + dept.getDeptId(),
                                newAncestors + "," + dept.getDeptId()));
                deptMapper.update(upd);
            }
        }
        return ok;
    }

    @Override
    public int deleteById(Long deptId) {
        long childCount = deptMapper.selectCountByCondition(SYS_DEPT.PARENT_ID.eq(deptId)
                .and(SYS_DEPT.DEL_FLAG.eq(Constants.NOT_DELETED)));
        if (childCount > 0) {
            throw new ServiceException("存在子部门，不允许删除");
        }
        long userCount = userMapper.selectCountByCondition(
                com.sudutech.system.domain.table.SysUserTableDef.SYS_USER.DEPT_ID.eq(deptId)
                        .and(com.sudutech.system.domain.table.SysUserTableDef.SYS_USER.DEL_FLAG.eq(Constants.NOT_DELETED)));
        if (userCount > 0) {
            throw new ServiceException("部门下存在用户，不允许删除");
        }
        return deptMapper.deleteById(deptId);
    }

    @Override
    public List<Long> selectDeptAndChildrenIds(Long deptId) {
        SysDept dept = deptMapper.selectOneById(deptId);
        if (dept == null) {
            return List.of(deptId);
        }
        String prefix = "0".equals(dept.getAncestors()) ? "0," + deptId : dept.getAncestors() + "," + deptId;
        List<SysDept> children = deptMapper.selectListByCondition(
                SYS_DEPT.ANCESTORS.likeLeft(prefix).or(SYS_DEPT.DEPT_ID.eq(deptId)));
        List<Long> ids = new ArrayList<>();
        children.forEach(d -> ids.add(d.getDeptId()));
        if (!ids.contains(deptId)) {
            ids.add(deptId);
        }
        return ids;
    }

    private List<SysDept> buildTree(List<SysDept> depts, Long root) {
        Map<Long, List<SysDept>> byParent = new HashMap<>();
        depts.forEach(d -> byParent.computeIfAbsent(d.getParentId(), k -> new ArrayList<>()).add(d));
        List<SysDept> tree = byParent.getOrDefault(root, new ArrayList<>());
        fillChildren(tree, byParent);
        return tree;
    }

    private void fillChildren(List<SysDept> nodes, Map<Long, List<SysDept>> byParent) {
        nodes.sort(Comparator.comparing(d -> d.getOrderNum() == null ? 0 : d.getOrderNum()));
        for (SysDept node : nodes) {
            List<SysDept> children = byParent.getOrDefault(node.getDeptId(), new ArrayList<>());
            node.setChildren(children);
            fillChildren(children, byParent);
        }
    }
}
