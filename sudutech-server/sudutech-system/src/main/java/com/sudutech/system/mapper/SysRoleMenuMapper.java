package com.sudutech.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sudutech.system.domain.SysRoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 角色菜单关联 Mapper
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    @Delete("delete from sys_role_menu where role_id = #{roleId}")
    int deleteRoleMenuByRoleId(@Param("roleId") Long roleId);
}
