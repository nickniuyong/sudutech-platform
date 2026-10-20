package com.sudutech.system.service;

import com.mybatisflex.core.paginate.Page;
import com.sudutech.common.core.page.PageQuery;
import com.sudutech.system.domain.SysDictData;
import com.sudutech.system.domain.SysDictType;

import java.util.List;

/**
 * 字典服务（Redis 缓存，变更主动失效）
 */
public interface SysDictTypeService {

    Page<SysDictType> pageList(SysDictType query, PageQuery pageQuery);

    List<SysDictType> selectAll();

    SysDictType selectById(Long dictId);

    boolean insert(SysDictType dictType);

    boolean update(SysDictType dictType);

    int deleteByIds(List<Long> dictIds);

    /** 按类型取字典数据（优先 Redis 缓存） */
    List<SysDictData> selectDictDataByType(String dictType);

    void insertDictData(SysDictData dictData);

    boolean updateDictData(SysDictData dictData);

    int deleteDictDataByIds(List<Long> dictCodes);

    Page<SysDictData> pageDictData(String dictType, PageQuery pageQuery);

    SysDictData selectDictDataById(Long dictCode);
}
