package com.sudutech.common.constant;

/**
 * 通用常量
 */
public class Constants {

    /** UTF-8 */
    public static final String UTF8 = "UTF-8";

    /** 令牌前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 超级管理员ID */
    public static final Long SUPER_ADMIN_ID = 1L;

    /** 超级管理员角色key */
    public static final String SUPER_ADMIN_KEY = "admin";

    /** 所有权限标识 */
    public static final String ALL_PERMISSION = "*:*:*";

    /** LoginUser 在 Sa-Token Session 中的 key */
    public static final String SESSION_LOGIN_USER = "loginUser";

    /** 逻辑删除-存在 */
    public static final String NOT_DELETED = "0";

    /** 逻辑删除-已删除 */
    public static final String DELETED = "2";

    /** 正常状态 */
    public static final String STATUS_NORMAL = "0";

    /** 停用状态 */
    public static final String STATUS_DISABLED = "1";

    private Constants() {
    }
}
