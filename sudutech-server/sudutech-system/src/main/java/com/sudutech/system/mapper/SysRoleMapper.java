package com.sudutech.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sudutech.system.domain.SysRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("select r.* from sys_role r " +
            "inner join sys_user_role ur on ur.role_id = r.role_id " +
            "where ur.user_id = #{userId} and r.del_flag = '0'")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    @Select("select r.* from sys_role r " +
            "inner join sys_role_menu rm on rm.role_id = r.role_id " +
            "where rm.menu_id = #{menuId} and r.del_flag = '0' and r.status = '0'")
    List<SysRole> selectRolesByMenuId(@Param("menuId") Long menuId);

    @Delete("delete from sys_role_menu where role_id = #{roleId}")
    int deleteRoleMenuByRoleId(@Param("roleId") Long roleId);

    @Delete("delete from sys_role_dept where role_id = #{roleId}")
    int deleteRoleDeptByRoleId(@Param("roleId") Long roleId);
}
