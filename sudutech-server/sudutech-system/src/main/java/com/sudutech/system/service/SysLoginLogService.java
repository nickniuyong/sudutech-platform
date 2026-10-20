package com.sudutech.system.service;

import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysLoginLog;

import java.util.List;

/**
 * 登录日志服务
 */
public interface SysLoginLogService {

    Page<SysLoginLog> pageList(SysLoginLog query, PageQuery pageQuery);

    int deleteByIds(List<Long> infoIds);

    void clean();

    void recordLogin(String userName, String status, String msg);
}
