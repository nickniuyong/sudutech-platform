package com.sudutech.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sudutech.system.domain.SysUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色关联 Mapper
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Delete("delete from sys_user_role where user_id = #{userId}")
    int deleteUserRoleByUserId(@Param("userId") Long userId);
}
