package com.sudutech.system.service;

import com.sudutech.system.domain.SysDept;

import java.util.List;

/**
 * 部门服务
 */
public interface SysDeptService {

    List<SysDept> selectDeptTree(SysDept query);

    List<SysDept> selectAll();

    SysDept selectById(Long deptId);

    boolean insertDept(SysDept dept);

    boolean updateDept(SysDept dept);

    int deleteById(Long deptId);

    /** 查询部门及其全部子部门ID */
    List<Long> selectDeptAndChildrenIds(Long deptId);
}
