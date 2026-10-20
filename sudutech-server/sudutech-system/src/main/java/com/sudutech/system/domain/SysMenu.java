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
 * 菜单权限表 sys_menu：目录M/菜单C/按钮F 三级
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_menu")
public class SysMenu extends BaseEntity {

    @Id(keyType = KeyType.Auto)
    private Long menuId;

    private String menuName;

    private Long parentId;

    private Integer orderNum;

    /** 路由地址 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 是否外链 0是 1否 */
    private Integer isFrame;

    /** M目录 C菜单 F按钮 */
    private String menuType;

    /** 显示 0显示 1隐藏 */
    private String visible;

    /** 状态 0正常 1停用 */
    private String status;

    /** 权限标识 */
    private String perms;

    private String icon;

    /** 非表字段 */
    @Column(ignore = true)
    private List<SysMenu> children;
}
