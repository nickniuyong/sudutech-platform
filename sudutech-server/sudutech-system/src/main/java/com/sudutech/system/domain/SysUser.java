package com.sudutech.system.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sudutech.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户表 sys_user
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_user")
public class SysUser extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long userId;

    private Long deptId;

    private String userName;

    private String nickName;

    private String email;

    private String phone;

    private String sex;

    private String avatar;

    private String password;

    private String status;

    @Column(isLogicDelete = true)
    private String delFlag;

    private String loginIp;

    private LocalDateTime loginDate;

    /** 非表字段 */
    @Column(ignore = true)
    private Long roleId;

    @Column(ignore = true)
    private List<Long> roleIds;

    @Column(ignore = true)
    private List<Long> postIds;

    @Column(ignore = true)
    private String deptName;

    @Column(ignore = true)
    private Boolean deptLeader;

    public boolean isAdmin() {
        return Long.valueOf(1L).equals(this.userId);
    }
}
