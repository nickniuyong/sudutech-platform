package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 角色部门关联 sys_role_dept（自定义数据权限）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("sys_role_dept")
public class SysRoleDept implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long roleId;

    @Id
    private Long deptId;
}
