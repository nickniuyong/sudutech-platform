package com.sudutech.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sudutech.system.domain.SysDept;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门 Mapper
 */
public interface SysDeptMapper extends BaseMapper<SysDept> {

    @Select("select d.* from sys_dept d " +
            "inner join sys_role_dept rd on rd.dept_id = d.dept_id " +
            "where rd.role_id = #{roleId} and d.del_flag = '0'")
    List<SysDept> selectDeptsByRoleId(@Param("roleId") Long roleId);
}
