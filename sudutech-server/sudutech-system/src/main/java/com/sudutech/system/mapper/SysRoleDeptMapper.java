package com.sudutech.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sudutech.system.domain.SysRoleDept;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 角色部门关联 Mapper
 */
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

    @Delete("delete from sys_role_dept where role_id = #{roleId}")
    int deleteRoleDeptByRoleId(@Param("roleId") Long roleId);
}
