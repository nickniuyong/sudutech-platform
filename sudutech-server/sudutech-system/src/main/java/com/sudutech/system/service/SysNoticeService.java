package com.sudutech.system.service;

import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysNotice;

/**
 * 通知公告服务
 */
public interface SysNoticeService {

    Page<SysNotice> pageList(SysNotice query, PageQuery pageQuery);

    SysNotice selectById(Long noticeId);

    boolean insert(SysNotice notice);

    boolean update(SysNotice notice);

    int deleteByIds(java.util.List<Long> noticeIds);
}
