package com.sudutech.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.core.domain.TableDataInfo;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysConfig;
import com.sudutech.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 参数配置
 */
@RestController
@RequestMapping("/api/v1/system/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;

    @SaCheckPermission("system:config:list")
    @GetMapping("/list")
    public TableDataInfo<SysConfig> list(SysConfig query, PageQuery pageQuery) {
        Page<SysConfig> page = configService.pageList(query, pageQuery);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckPermission("system:config:query")
    @GetMapping("/{configId}")
    public R<SysConfig> getInfo(@PathVariable Long configId) {
        return R.ok(configService.selectById(configId));
    }

    @SaCheckPermission("system:config:add")
    @Log(title = "参数配置", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysConfig config) {
        configService.insert(config);
        return R.ok("新增参数成功", null);
    }

    @SaCheckPermission("system:config:edit")
    @Log(title = "参数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysConfig config) {
        configService.update(config);
        return R.ok("修改参数成功", null);
    }

    @SaCheckPermission("system:config:remove")
    @Log(title = "参数配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public R<Void> remove(@PathVariable String configIds) {
        List<Long> ids = Arrays.stream(configIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
        configService.deleteByIds(ids);
        return R.ok("删除成功", null);
    }

    @GetMapping("/key/{configKey}")
    public R<String> byKey(@PathVariable String configKey) {
        return R.ok(configService.getConfigValue(configKey, ""));
    }
}
