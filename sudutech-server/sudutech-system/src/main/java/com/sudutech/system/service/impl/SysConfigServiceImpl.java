package com.sudutech.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.JsonUtils;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysConfig;
import com.sudutech.system.domain.table.SysConfigTableDef;
import com.sudutech.system.mapper.SysConfigMapper;
import com.sudutech.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static com.sudutech.system.domain.table.SysConfigTableDef.SYS_CONFIG;

/**
 * 参数配置服务实现：Redis 缓存 key = sudutech:config:{key}
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private static final String CACHE_PREFIX = "sudutech:config:";

    private final SysConfigMapper configMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Page<SysConfig> pageList(SysConfig query, PageQuery pageQuery) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query != null) {
            if (StringUtils.isNotBlank(query.getConfigName())) {
                wrapper.and(SYS_CONFIG.CONFIG_NAME.like(query.getConfigName()));
            }
            if (StringUtils.isNotBlank(query.getConfigKey())) {
                wrapper.and(SYS_CONFIG.CONFIG_KEY.like(query.getConfigKey()));
            }
            if (StringUtils.isNotBlank(query.getConfigType())) {
                wrapper.and(SYS_CONFIG.CONFIG_TYPE.eq(query.getConfigType()));
            }
        }
        wrapper.orderBy(SYS_CONFIG.CONFIG_ID.asc());
        return configMapper.paginate(pageQuery.toPage(), wrapper);
    }

    @Override
    public List<SysConfig> selectAll() {
        return configMapper.selectAll();
    }

    @Override
    public SysConfig selectById(Long configId) {
        return configMapper.selectOneById(configId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insert(SysConfig config) {
        checkUnique(config);
        config.setConfigId(null);
        boolean ok = configMapper.insert(config) > 0;
        if (ok) {
            evict(config.getConfigKey());
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysConfig config) {
        SysConfig old = configMapper.selectOneById(config.getConfigId());
        if (old == null) {
            throw new ServiceException("参数不存在");
        }
        if ("Y".equals(old.getConfigType()) && StringUtils.isNotBlank(config.getConfigKey())
                && !old.getConfigKey().equals(config.getConfigKey())) {
            throw new ServiceException("内置参数不允许修改键名");
        }
        checkUnique(config);
        boolean ok = configMapper.update(config) > 0;
        if (ok) {
            evict(old.getConfigKey());
            evict(config.getConfigKey());
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> configIds) {
        int count = 0;
        for (Long id : configIds) {
            SysConfig config = configMapper.selectOneById(id);
            if (config == null) {
                continue;
            }
            if ("Y".equals(config.getConfigType())) {
                throw new ServiceException("内置参数[" + config.getConfigName() + "]不允许删除");
            }
            count += configMapper.deleteById(id);
            evict(config.getConfigKey());
        }
        return count;
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        String key = CACHE_PREFIX + configKey;
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return cached;
            }
        } catch (Exception ignore) {
        }
        SysConfig config = configMapper.selectOneByCondition(SYS_CONFIG.CONFIG_KEY.eq(configKey));
        String value = config != null ? config.getConfigValue() : defaultValue;
        if (config != null) {
            try {
                redisTemplate.opsForValue().set(key, value == null ? "" : value, Duration.ofHours(12));
            } catch (Exception ignore) {
            }
        }
        return value;
    }

    @Override
    public boolean isEnabled(String configKey) {
        return Boolean.parseBoolean(getConfigValue(configKey, "false"));
    }

    private void checkUnique(SysConfig config) {
        SysConfig exist = configMapper.selectOneByCondition(SYS_CONFIG.CONFIG_KEY.eq(config.getConfigKey()));
        if (exist != null && (config.getConfigId() == null || !exist.getConfigId().equals(config.getConfigId()))) {
            throw new ServiceException("参数键名已存在: " + config.getConfigKey());
        }
    }

    private void evict(String configKey) {
        try {
            redisTemplate.delete(CACHE_PREFIX + configKey);
        } catch (Exception ignore) {
        }
    }
}
