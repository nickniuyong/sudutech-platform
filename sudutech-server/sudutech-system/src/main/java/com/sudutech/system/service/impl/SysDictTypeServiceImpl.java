package com.sudutech.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.constant.Constants;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.JsonUtils;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysDictData;
import com.sudutech.system.domain.SysDictType;
import com.sudutech.system.domain.table.SysDictDataTableDef;
import com.sudutech.system.domain.table.SysDictTypeTableDef;
import com.sudutech.system.mapper.SysDictDataMapper;
import com.sudutech.system.mapper.SysDictTypeMapper;
import com.sudutech.system.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static com.sudutech.system.domain.table.SysDictTypeTableDef.SYS_DICT_TYPE;

/**
 * 字典服务实现：Redis 缓存 key = dict:{type}，变更后主动删除缓存
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl implements SysDictTypeService {

    private static final String CACHE_PREFIX = "sudutech:dict:";

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Page<SysDictType> pageList(SysDictType query, PageQuery pageQuery) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query != null) {
            if (StringUtils.isNotBlank(query.getDictName())) {
                wrapper.and(SYS_DICT_TYPE.DICT_NAME.like(query.getDictName()));
            }
            if (StringUtils.isNotBlank(query.getDictType())) {
                wrapper.and(SYS_DICT_TYPE.DICT_TYPE.like(query.getDictType()));
            }
            if (StringUtils.isNotBlank(query.getStatus())) {
                wrapper.and(SYS_DICT_TYPE.STATUS.eq(query.getStatus()));
            }
        }
        wrapper.orderBy(SYS_DICT_TYPE.DICT_ID.asc());
        return dictTypeMapper.paginate(pageQuery.toPage(), wrapper);
    }

    @Override
    public List<SysDictType> selectAll() {
        return dictTypeMapper.selectAll();
    }

    @Override
    public SysDictType selectById(Long dictId) {
        return dictTypeMapper.selectOneById(dictId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insert(SysDictType dictType) {
        checkUnique(dictType);
        dictType.setDictId(null);
        boolean ok = dictTypeMapper.insert(dictType) > 0;
        if (ok) {
            evict(dictType.getDictType());
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysDictType dictType) {
        SysDictType old = dictTypeMapper.selectOneById(dictType.getDictId());
        if (old == null) {
            throw new ServiceException("字典类型不存在");
        }
        checkUnique(dictType);
        boolean ok = dictTypeMapper.update(dictType) > 0;
        if (ok && !old.getDictType().equals(dictType.getDictType())) {
            // 类型名变更，同步字典数据
            SysDictData data = new SysDictData();
            data.setDictType(dictType.getDictType());
            dictDataMapper.updateByQuery(data,
                    QueryWrapper.create().where(SysDictDataTableDef.SYS_DICT_DATA.DICT_TYPE.eq(old.getDictType())));
            evict(old.getDictType());
        }
        evict(dictType.getDictType());
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> dictIds) {
        int count = 0;
        for (Long dictId : dictIds) {
            SysDictType type = dictTypeMapper.selectOneById(dictId);
            if (type == null) {
                continue;
            }
            long used = dictDataMapper.selectCountByCondition(
                    SysDictDataTableDef.SYS_DICT_DATA.DICT_TYPE.eq(type.getDictType()));
            if (used > 0) {
                throw new ServiceException("字典[" + type.getDictName() + "]下存在数据，不允许删除");
            }
            count += dictTypeMapper.deleteById(dictId);
            evict(type.getDictType());
        }
        return count;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SysDictData> selectDictDataByType(String dictType) {
        String key = CACHE_PREFIX + dictType;
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(cached)) {
                return JsonUtils.parseList(cached, SysDictData.class);
            }
        } catch (Exception ignore) {
            // Redis 不可用时降级查库
        }
        List<SysDictData> data = dictDataMapper.selectListByCondition(
                SysDictDataTableDef.SYS_DICT_DATA.DICT_TYPE.eq(dictType)
                        .and(SysDictDataTableDef.SYS_DICT_DATA.STATUS.eq(Constants.STATUS_NORMAL)))
                .stream().sorted(java.util.Comparator.comparing(d -> d.getDictSort() == null ? 0 : d.getDictSort()))
                .toList();
        try {
            redisTemplate.opsForValue().set(key, JsonUtils.toJson(data), Duration.ofHours(12));
        } catch (Exception ignore) {
        }
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertDictData(SysDictData dictData) {
        dictData.setDictCode(null);
        dictDataMapper.insert(dictData);
        evict(dictData.getDictType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictData(SysDictData dictData) {
        SysDictData old = dictDataMapper.selectOneById(dictData.getDictCode());
        boolean ok = dictDataMapper.update(dictData) > 0;
        if (ok) {
            evict(old != null ? old.getDictType() : dictData.getDictType());
            evict(dictData.getDictType());
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDictDataByIds(List<Long> dictCodes) {
        int count = 0;
        for (Long code : dictCodes) {
            SysDictData data = dictDataMapper.selectOneById(code);
            count += dictDataMapper.deleteById(code);
            if (data != null) {
                evict(data.getDictType());
            }
        }
        return count;
    }

    @Override
    public Page<SysDictData> pageDictData(String dictType, PageQuery pageQuery) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SysDictDataTableDef.SYS_DICT_DATA.DICT_TYPE.eq(dictType))
                .orderBy(SysDictDataTableDef.SYS_DICT_DATA.DICT_SORT.asc());
        return dictDataMapper.paginate(pageQuery.toPage(), wrapper);
    }

    @Override
    public SysDictData selectDictDataById(Long dictCode) {
        return dictDataMapper.selectOneById(dictCode);
    }

    private void checkUnique(SysDictType dictType) {
        SysDictType exist = dictTypeMapper.selectOneByCondition(
                SYS_DICT_TYPE.DICT_TYPE.eq(dictType.getDictType()));
        if (exist != null && (dictType.getDictId() == null
                || !exist.getDictId().equals(dictType.getDictId()))) {
            throw new ServiceException("字典类型已存在: " + dictType.getDictType());
        }
    }

    private void evict(String dictType) {
        try {
            redisTemplate.delete(CACHE_PREFIX + dictType);
        } catch (Exception ignore) {
        }
    }
}
