package com.sudutech.common.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.sudutech.common.constant.Constants;
import com.sudutech.common.core.domain.LoginUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全相关工具：密码加密、当前登录用户
 */
public class SecurityUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    public static String encodePassword(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /** 获取当前登录用户（未登录返回 null） */
    public static LoginUser getLoginUser() {
        return LoginHelper.getLoginUser();
    }

    /** 获取当前用户ID */
    public static Long getUserId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUserId() : null;
    }

    /** 获取当前用户名 */
    public static String getUsername() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUserName() : "";
    }

    /** 是否超级管理员 */
    public static boolean isAdmin(Long userId) {
        return Constants.SUPER_ADMIN_ID.equals(userId);
    }

    /** 校验密码强度：8-20位，包含字母与数字 */
    public static void checkPasswordStrength(String password) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            throw new IllegalArgumentException("密码长度须为8-20位");
        }
        if (!password.matches(".*[a-zA-Z]+.*") || !password.matches(".*\\d+.*")) {
            throw new IllegalArgumentException("密码必须包含字母和数字");
        }
    }

    private SecurityUtils() {
    }
}
