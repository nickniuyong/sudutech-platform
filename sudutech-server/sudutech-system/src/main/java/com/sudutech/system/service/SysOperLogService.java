package com.sudutech.system.service;

import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysOperLog;

import java.util.List;

/**
 * 操作日志服务
 */
public interface SysOperLogService {

    Page<SysOperLog> pageList(SysOperLog query, PageQuery pageQuery);

    int deleteByIds(List<Long> operIds);

    void clean();
}
