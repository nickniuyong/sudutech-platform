package com.sudutech.screen.controller;

import com.sudutech.common.core.domain.R;
import com.sudutech.screen.service.ScreenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 大屏发布态访问（匿名，路由已在 Sa-Token 白名单放行 /screen/view/**）
 */
@RestController
@RequestMapping("/api/v1/screen/view")
@RequiredArgsConstructor
public class ScreenViewController {

    private final ScreenService screenService;

    /** 按分享标识读取大屏（只读） */
    @GetMapping("/{publishUrl}")
    public R<java.util.Map<String, Object>> view(@PathVariable String publishUrl) {
        return R.ok(screenService.viewByPublishUrl(publishUrl));
    }
}
