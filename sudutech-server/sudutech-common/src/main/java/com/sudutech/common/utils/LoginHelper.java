package com.sudutech.common.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.sudutech.common.constant.Constants;
import com.sudutech.common.core.domain.LoginUser;

/**
 * 登录态辅助：从 Sa-Token Session 读写 LoginUser
 */
public class LoginHelper {

    /** 保存登录用户到会话（登录时调用） */
    public static void login(LoginUser loginUser) {
        StpUtil.login(loginUser.getUserId());
        StpUtil.getSession().set(Constants.SESSION_LOGIN_USER, loginUser);
    }

    /** 获取当前登录用户 */
    public static LoginUser getLoginUser() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            Object obj = StpUtil.getSession().get(Constants.SESSION_LOGIN_USER);
            return obj instanceof LoginUser ? (LoginUser) obj : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** 未登录时抛出异常 */
    public static LoginUser getLoginUserRequired() {
        LoginUser user = getLoginUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        return user;
    }

    /** 登出 */
    public static void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    private LoginHelper() {
    }
}
