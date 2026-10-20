package com.sudutech.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.common.core.service.OperLogRecorder;
import com.sudutech.common.utils.LoginHelper;
import com.sudutech.common.utils.ServletUtils;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysOperLog;
import com.sudutech.system.domain.table.SysOperLogTableDef;
import com.sudutech.system.mapper.SysOperLogMapper;
import com.sudutech.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.sudutech.system.domain.table.SysOperLogTableDef.SYS_OPER_LOG;

/**
 * 操作日志服务实现（同时作为 LogAspect 的落库回调）
 */
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl implements SysOperLogService, OperLogRecorder {

    private final SysOperLogMapper operLogMapper;

    @Override
    public Page<SysOperLog> pageList(SysOperLog query, PageQuery pageQuery) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query != null) {
            if (StringUtils.isNotBlank(query.getTitle())) {
                wrapper.and(SYS_OPER_LOG.TITLE.like(query.getTitle()));
            }
            if (StringUtils.isNotBlank(query.getOperName())) {
                wrapper.and(SYS_OPER_LOG.OPER_NAME.like(query.getOperName()));
            }
            if (query.getBusinessType() != null && query.getBusinessType() >= 0) {
                wrapper.and(SYS_OPER_LOG.BUSINESS_TYPE.eq(query.getBusinessType()));
            }
            if (query.getStatus() != null && query.getStatus() >= 0) {
                wrapper.and(SYS_OPER_LOG.STATUS.eq(query.getStatus()));
            }
        }
        wrapper.orderBy(SYS_OPER_LOG.OPER_ID.desc());
        return operLogMapper.paginate(pageQuery.toPage(), wrapper);
    }

    @Override
    public int deleteByIds(List<Long> operIds) {
        return operLogMapper.deleteBatchByIds(operIds);
    }

    @Override
    public void clean() {
        operLogMapper.deleteByQuery(com.mybatisflex.core.query.QueryWrapper.create());
    }

    @Async
    @Override
    public void record(String title, int businessType, String method, String requestMethod,
                       String operUrl, String operParam, String jsonResult,
                       int status, String errorMsg, long costTime) {
        SysOperLog log = new SysOperLog();
        log.setTitle(title);
        log.setBusinessType(businessType);
        log.setMethod(method);
        log.setRequestMethod(requestMethod);
        log.setOperatorType(1);
        log.setOperName(LoginHelper.getLoginUser() != null ? LoginHelper.getLoginUser().getUserName() : "");
        log.setOperUrl(operUrl);
        log.setOperIp(ServletUtils.getClientIp(ServletUtils.getRequest()));
        log.setOperParam(operParam);
        log.setJsonResult(jsonResult);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        log.setCostTime(costTime);
        log.setOperTime(LocalDateTime.now());
        operLogMapper.insert(log);
    }
}
