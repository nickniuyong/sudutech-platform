package com.sudutech.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysNotice;
import com.sudutech.system.domain.table.SysNoticeTableDef;
import com.sudutech.system.mapper.SysNoticeMapper;
import com.sudutech.system.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sudutech.system.domain.table.SysNoticeTableDef.SYS_NOTICE;

/**
 * 通知公告服务实现
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl implements SysNoticeService {

    private final SysNoticeMapper noticeMapper;

    @Override
    public Page<SysNotice> pageList(SysNotice query, PageQuery pageQuery) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query != null) {
            if (StringUtils.isNotBlank(query.getNoticeTitle())) {
                wrapper.and(SYS_NOTICE.NOTICE_TITLE.like(query.getNoticeTitle()));
            }
            if (StringUtils.isNotBlank(query.getNoticeType())) {
                wrapper.and(SYS_NOTICE.NOTICE_TYPE.eq(query.getNoticeType()));
            }
            if (StringUtils.isNotBlank(query.getCreateBy())) {
                wrapper.and(SYS_NOTICE.CREATE_BY.like(query.getCreateBy()));
            }
        }
        wrapper.orderBy(SYS_NOTICE.NOTICE_ID.desc());
        return noticeMapper.paginate(pageQuery.toPage(), wrapper);
    }

    @Override
    public SysNotice selectById(Long noticeId) {
        return noticeMapper.selectOneById(noticeId);
    }

    @Override
    public boolean insert(SysNotice notice) {
        notice.setNoticeId(null);
        return noticeMapper.insert(notice) > 0;
    }

    @Override
    public boolean update(SysNotice notice) {
        return noticeMapper.update(notice) > 0;
    }

    @Override
    public int deleteByIds(List<Long> noticeIds) {
        return noticeMapper.deleteBatchByIds(noticeIds);
    }
}
