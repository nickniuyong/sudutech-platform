package com.sudutech.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.core.domain.TableDataInfo;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysOperLog;
import com.sudutech.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志
 */
@RestController
@RequestMapping("/api/v1/system/operlog")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService operLogService;

    @SaCheckPermission("system:oper:list")
    @GetMapping("/list")
    public TableDataInfo<SysOperLog> list(SysOperLog query, PageQuery pageQuery) {
        Page<SysOperLog> page = operLogService.pageList(query, pageQuery);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckPermission("system:oper:remove")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{operIds}")
    public R<Void> remove(@PathVariable String operIds) {
        List<Long> ids = Arrays.stream(operIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
        operLogService.deleteByIds(ids);
        return R.ok("删除成功", null);
    }

    @SaCheckPermission("system:oper:remove")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogService.clean();
        return R.ok("清空成功", null);
    }
}
