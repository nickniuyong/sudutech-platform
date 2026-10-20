package com.sudutech.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.core.domain.TableDataInfo;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysNotice;
import com.sudutech.system.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知公告
 */
@RestController
@RequestMapping("/api/v1/system/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeService noticeService;

    @SaCheckPermission("system:notice:list")
    @GetMapping("/list")
    public TableDataInfo<SysNotice> list(SysNotice query, PageQuery pageQuery) {
        Page<SysNotice> page = noticeService.pageList(query, pageQuery);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckPermission("system:notice:query")
    @GetMapping("/{noticeId}")
    public R<SysNotice> getInfo(@PathVariable Long noticeId) {
        return R.ok(noticeService.selectById(noticeId));
    }

    @SaCheckPermission("system:notice:add")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysNotice notice) {
        noticeService.insert(notice);
        return R.ok("新增公告成功", null);
    }

    @SaCheckPermission("system:notice:edit")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysNotice notice) {
        noticeService.update(notice);
        return R.ok("修改公告成功", null);
    }

    @SaCheckPermission("system:notice:remove")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public R<Void> remove(@PathVariable String noticeIds) {
        List<Long> ids = Arrays.stream(noticeIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
        noticeService.deleteByIds(ids);
        return R.ok("删除成功", null);
    }
}
