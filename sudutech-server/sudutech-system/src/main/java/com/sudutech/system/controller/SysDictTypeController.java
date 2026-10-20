package com.sudutech.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.core.domain.TableDataInfo;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysDictData;
import com.sudutech.system.domain.SysDictType;
import com.sudutech.system.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典管理
 */
@RestController
@RequestMapping("/api/v1/system/dict")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final SysDictTypeService dictTypeService;

    // ---------- 字典类型 ----------
    @SaCheckPermission("system:dict:list")
    @GetMapping("/type/list")
    public TableDataInfo<SysDictType> typeList(SysDictType query, PageQuery pageQuery) {
        Page<SysDictType> page = dictTypeService.pageList(query, pageQuery);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckPermission("system:dict:query")
    @GetMapping("/type/{dictId}")
    public R<SysDictType> typeInfo(@PathVariable Long dictId) {
        return R.ok(dictTypeService.selectById(dictId));
    }

    @SaCheckPermission("system:dict:add")
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    @PostMapping("/type")
    public R<Void> addType(@RequestBody SysDictType dictType) {
        dictTypeService.insert(dictType);
        return R.ok("新增字典类型成功", null);
    }

    @SaCheckPermission("system:dict:edit")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    @PutMapping("/type")
    public R<Void> editType(@RequestBody SysDictType dictType) {
        dictTypeService.update(dictType);
        return R.ok("修改字典类型成功", null);
    }

    @SaCheckPermission("system:dict:remove")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/type/{dictIds}")
    public R<Void> removeType(@PathVariable String dictIds) {
        List<Long> ids = Arrays.stream(dictIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
        dictTypeService.deleteByIds(ids);
        return R.ok("删除成功", null);
    }

    /** 按类型取字典数据（走 Redis 缓存） */
    @GetMapping("/data/type/{dictType}")
    public R<List<SysDictData>> dataByType(@PathVariable String dictType) {
        return R.ok(dictTypeService.selectDictDataByType(dictType));
    }

    // ---------- 字典数据 ----------
    @SaCheckPermission("system:dict:list")
    @GetMapping("/data/list")
    public TableDataInfo<SysDictData> dataList(@RequestParam String dictType, PageQuery pageQuery) {
        Page<SysDictData> page = dictTypeService.pageDictData(dictType, pageQuery);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckPermission("system:dict:query")
    @GetMapping("/data/{dictCode}")
    public R<SysDictData> dataInfo(@PathVariable Long dictCode) {
        return R.ok(dictTypeService.selectDictDataById(dictCode));
    }

    @SaCheckPermission("system:dict:add")
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    @PostMapping("/data")
    public R<Void> addData(@RequestBody SysDictData dictData) {
        dictTypeService.insertDictData(dictData);
        return R.ok("新增字典数据成功", null);
    }

    @SaCheckPermission("system:dict:edit")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @PutMapping("/data")
    public R<Void> editData(@RequestBody SysDictData dictData) {
        dictTypeService.updateDictData(dictData);
        return R.ok("修改字典数据成功", null);
    }

    @SaCheckPermission("system:dict:remove")
    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/data/{dictCodes}")
    public R<Void> removeData(@PathVariable String dictCodes) {
        List<Long> ids = Arrays.stream(dictCodes.split(",")).map(Long::valueOf).collect(Collectors.toList());
        dictTypeService.deleteDictDataByIds(ids);
        return R.ok("删除成功", null);
    }
}
