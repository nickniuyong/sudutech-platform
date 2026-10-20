package com.sudutech.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.core.domain.TableDataInfo;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysLoginLog;
import com.sudutech.system.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录日志
 */
@RestController
@RequestMapping("/api/v1/system/loginlog")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogService loginLogService;

    @SaCheckPermission("system:loginlog:list")
    @GetMapping("/list")
    public TableDataInfo<SysLoginLog> list(SysLoginLog query, PageQuery pageQuery) {
        Page<SysLoginLog> page = loginLogService.pageList(query, pageQuery);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckPermission("system:loginlog:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public R<Void> remove(@PathVariable String infoIds) {
        List<Long> ids = Arrays.stream(infoIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
        loginLogService.deleteByIds(ids);
        return R.ok("删除成功", null);
    }

    @SaCheckPermission("system:loginlog:remove")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        loginLogService.clean();
        return R.ok("清空成功", null);
    }
}
