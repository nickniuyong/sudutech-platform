package com.sudutech.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.common.utils.ServletUtils;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysLoginLog;
import com.sudutech.system.domain.table.SysLoginLogTableDef;
import com.sudutech.system.mapper.SysLoginLogMapper;
import com.sudutech.system.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.sudutech.system.domain.table.SysLoginLogTableDef.SYS_LOGIN_LOG;

/**
 * 登录日志服务实现
 */
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl implements SysLoginLogService {

    private final SysLoginLogMapper loginLogMapper;

    @Override
    public Page<SysLoginLog> pageList(SysLoginLog query, PageQuery pageQuery) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query != null) {
            if (StringUtils.isNotBlank(query.getUserName())) {
                wrapper.and(SYS_LOGIN_LOG.USER_NAME.like(query.getUserName()));
            }
            if (StringUtils.isNotBlank(query.getIpaddr())) {
                wrapper.and(SYS_LOGIN_LOG.IPADDR.like(query.getIpaddr()));
            }
            if (StringUtils.isNotBlank(query.getStatus())) {
                wrapper.and(SYS_LOGIN_LOG.STATUS.eq(query.getStatus()));
            }
        }
        wrapper.orderBy(SYS_LOGIN_LOG.INFO_ID.desc());
        return loginLogMapper.paginate(pageQuery.toPage(), wrapper);
    }

    @Override
    public int deleteByIds(List<Long> infoIds) {
        return loginLogMapper.deleteBatchByIds(infoIds);
    }

    @Override
    public void clean() {
        loginLogMapper.deleteByQuery(com.mybatisflex.core.query.QueryWrapper.create());
    }

    @Override
    public void recordLogin(String userName, String status, String msg) {
        SysLoginLog log = new SysLoginLog();
        log.setUserName(userName);
        log.setIpaddr(ServletUtils.getClientIp(ServletUtils.getRequest()));
        log.setBrowser("");
        log.setOs("");
        log.setStatus(status);
        log.setMsg(msg);
        log.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(log);
    }
}
