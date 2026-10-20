package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户角色关联 sys_user_role
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("sys_user_role")
public class SysUserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long userId;

    @Id
    private Long roleId;
}
