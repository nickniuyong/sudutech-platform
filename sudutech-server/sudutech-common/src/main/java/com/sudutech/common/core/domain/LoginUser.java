package com.sudutech.common.core.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 登录用户信息（存入 Sa-Token Session）
 */
@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userName;
    private String nickName;
    private Long deptId;
    private String deptName;
    /** 角色 key 集合 */
    private Set<String> roleKeys = new HashSet<>();
    /** 数据权限范围 1全部 2自定义 3本部门 4本部门及以下 5仅本人 */
    private String dataScope = "1";

    public boolean isAdmin() {
        return Long.valueOf(1L).equals(this.userId);
    }

    public boolean hasRole(String roleKey) {
        return roleKeys != null && roleKeys.contains(roleKey);
    }
}
