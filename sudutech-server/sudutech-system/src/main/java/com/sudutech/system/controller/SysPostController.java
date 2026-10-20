package com.sudutech.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.annotation.BusinessType;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.domain.R;
import com.sudutech.common.core.domain.TableDataInfo;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysPost;
import com.sudutech.system.domain.table.SysPostTableDef;
import com.sudutech.system.mapper.SysPostMapper;
import com.sudutech.system.service.SysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.sudutech.system.domain.table.SysPostTableDef.SYS_POST;

/**
 * 岗位管理
 */
@RestController
@RequestMapping("/api/v1/system/post")
@RequiredArgsConstructor
public class SysPostController {

    private final SysPostService postService;
    private final SysPostMapper postMapper;

    @SaCheckPermission("system:post:list")
    @GetMapping("/list")
    public TableDataInfo<SysPost> list(SysPost query, PageQuery pageQuery) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query.getPostName() != null && !query.getPostName().isBlank()) {
            wrapper.and(SYS_POST.POST_NAME.like(query.getPostName()));
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.and(SYS_POST.STATUS.eq(query.getStatus()));
        }
        wrapper.orderBy(SYS_POST.POST_SORT.asc());
        Page<SysPost> page = postMapper.paginate(pageQuery.toPage(), wrapper);
        return TableDataInfo.build(page.getTotalRow(), page.getRecords());
    }

    @SaCheckPermission("system:post:query")
    @GetMapping("/{postId}")
    public R<SysPost> getInfo(@PathVariable Long postId) {
        return R.ok(postService.selectById(postId));
    }

    @SaCheckPermission("system:post:add")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysPost post) {
        postService.insert(post);
        return R.ok("新增岗位成功", null);
    }

    @SaCheckPermission("system:post:edit")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysPost post) {
        postService.update(post);
        return R.ok("修改岗位成功", null);
    }

    @SaCheckPermission("system:post:remove")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    public R<Void> remove(@PathVariable String postIds) {
        List<Long> ids = Arrays.stream(postIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
        postService.deleteByIds(ids);
        return R.ok("删除成功", null);
    }
}
