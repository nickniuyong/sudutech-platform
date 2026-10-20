package com.sudutech.system.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 角色表 sys_role
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_role")
public class SysRole extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long roleId;

    private String roleName;

    private String roleKey;

    private Integer roleSort;

    /** 数据范围 1全部 2自定义 3本部门 4本部门及以下 5仅本人 */
    private String dataScope;

    private String status;

    @Column(isLogicDelete = true)
    private String delFlag;

    /** 非表字段 */
    @Column(ignore = true)
    private List<Long> menuIds;

    @Column(ignore = true)
    private List<Long> deptIds;

    public boolean isAdmin() {
        return Long.valueOf(1L).equals(this.roleId);
    }
}
