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
 * 部门表 sys_dept（树形）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_dept")
public class SysDept extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long deptId;

    private Long parentId;

    /** 祖级列表 如 0,100,101 */
    private String ancestors;

    private String deptName;

    private Integer orderNum;

    private Long leaderUserId;

    private String phone;

    private String email;

    private String status;

    @Column(isLogicDelete = true)
    private String delFlag;

    /** 非表字段 */
    @Column(ignore = true)
    private String leaderName;

    @Column(ignore = true)
    private List<SysDept> children;
}
