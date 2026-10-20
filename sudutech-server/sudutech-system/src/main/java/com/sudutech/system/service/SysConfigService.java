package com.sudutech.system.service;

import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysConfig;

import java.util.List;

/**
 * 参数配置服务（Redis 缓存）
 */
public interface SysConfigService {

    Page<SysConfig> pageList(SysConfig query, PageQuery pageQuery);

    List<SysConfig> selectAll();

    SysConfig selectById(Long configId);

    boolean insert(SysConfig config);

    boolean update(SysConfig config);

    int deleteByIds(List<Long> configIds);

    /** 读取参数值（优先缓存） */
    String getConfigValue(String configKey, String defaultValue);

    /** 是否开启某开关 */
    boolean isEnabled(String configKey);
}
