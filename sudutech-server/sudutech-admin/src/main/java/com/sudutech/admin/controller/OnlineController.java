package com.sudutech.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在线用户监控（基于 Sa-Token 会话）
 */
@RestController
@RequestMapping("/api/v1/monitor/online")
@RequiredArgsConstructor
public class OnlineController {

    private final SysUserService userService;

    @SaCheckPermission("monitor:online:list")
    @GetMapping("/list")
    public R<List<Map<String, Object>>> list(@RequestParam(required = false) String userName) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String token : StpUtil.searchTokenValue("", 0, -1, false)) {
            try {
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId == null) {
                    continue;
                }
                cn.dev33.satoken.session.SaSession session = StpUtil.getSessionByLoginId(loginId, false);
                if (session == null) {
                    continue;
                }
                Object lu = session.get("loginUser");
                if (!(lu instanceof com.sudutech.common.core.domain.LoginUser user)) {
                    continue;
                }
                if (userName != null && !userName.isBlank() && !user.getUserName().contains(userName)) {
                    continue;
                }
                Map<String, Object> item = new HashMap<>();
                item.put("tokenId", token);
                item.put("userId", user.getUserId());
                item.put("userName", user.getUserName());
                item.put("nickName", user.getNickName());
                item.put("deptName", user.getDeptName());
                item.put("remainSeconds", StpUtil.getTokenTimeout(token));
                list.add(item);
            } catch (Exception ignore) {
            }
        }
        return R.ok(list);
    }

    @SaCheckPermission("monitor:online:list")
    @DeleteMapping("/{tokenId}")
    public R<Void> forceLogout(@PathVariable String tokenId) {
        try {
            StpUtil.logoutByTokenValue(tokenId);
        } catch (Exception e) {
            throw new ServiceException("强退失败");
        }
        return R.ok("强退成功", null);
    }
}
