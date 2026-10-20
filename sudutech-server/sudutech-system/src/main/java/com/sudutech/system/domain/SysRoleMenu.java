package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 角色菜单关联 sys_role_menu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("sys_role_menu")
public class SysRoleMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long roleId;

    @Id
    private Long menuId;
}
