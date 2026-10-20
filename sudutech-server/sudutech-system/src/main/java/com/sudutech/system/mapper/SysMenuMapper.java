package com.sudutech.system.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sudutech.system.domain.SysMenu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("select distinct m.* from sys_menu m " +
            "inner join sys_role_menu rm on rm.menu_id = m.menu_id " +
            "inner join sys_user_role ur on ur.role_id = rm.role_id " +
            "where ur.user_id = #{userId} and m.status = '0' order by m.parent_id, m.order_num")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    @Select("select m.perms from sys_menu m " +
            "inner join sys_role_menu rm on rm.menu_id = m.menu_id " +
            "inner join sys_user_role ur on ur.role_id = rm.role_id " +
            "inner join sys_role r on r.role_id = ur.role_id " +
            "where ur.user_id = #{userId} and r.status = '0' and r.del_flag = '0' and m.perms is not null")
    List<String> selectPermsByUserId(@Param("userId") Long userId);
}
