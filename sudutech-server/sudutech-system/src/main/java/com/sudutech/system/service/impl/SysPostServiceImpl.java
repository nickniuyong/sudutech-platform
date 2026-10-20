package com.sudutech.system.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.constant.Constants;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysPost;
import com.sudutech.system.domain.table.SysPostTableDef;
import com.sudutech.system.mapper.SysPostMapper;
import com.sudutech.system.service.SysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sudutech.system.domain.table.SysPostTableDef.SYS_POST;

/**
 * 岗位服务实现
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl implements SysPostService {

    private final SysPostMapper postMapper;

    @Override
    public List<SysPost> selectAll() {
        return postMapper.selectListByQuery(QueryWrapper.create()
                .where(SYS_POST.STATUS.eq(Constants.STATUS_NORMAL))
                .orderBy(SYS_POST.POST_SORT.asc()));
    }

    @Override
    public SysPost selectById(Long postId) {
        return postMapper.selectOneById(postId);
    }

    @Override
    public boolean insert(SysPost post) {
        post.setPostId(null);
        return postMapper.insert(post) > 0;
    }

    @Override
    public boolean update(SysPost post) {
        return postMapper.update(post) > 0;
    }

    @Override
    public int deleteByIds(List<Long> postIds) {
        return postMapper.deleteBatchByIds(postIds);
    }
}
