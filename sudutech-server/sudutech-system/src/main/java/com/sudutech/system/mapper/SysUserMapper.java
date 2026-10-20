package com.sudutech.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sudutech.system.domain.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("select u.* from sys_user u " +
            "inner join sys_user_role ur on u.user_id = ur.user_id " +
            "inner join sys_role r on r.role_id = ur.role_id " +
            "where r.role_key = #{roleKey} and r.del_flag = '0' and u.del_flag = '0'")
    List<SysUser> selectUsersByRoleKey(@Param("roleKey") String roleKey);

    @Select("select count(1) from sys_user where dept_id = #{deptId} and del_flag = '0'")
    long countByDeptId(@Param("deptId") Long deptId);

    @Delete("delete from sys_user_role where user_id = #{userId}")
    int deleteUserRoleByUserId(@Param("userId") Long userId);
}
