package com.sudutech.gen.service;

import com.sudutech.gen.domain.GenTable;

import java.util.List;
import java.util.Map;

/**
 * 代码生成服务
 */
public interface GenService {

    /** 查询当前库全部数据表（来自 information_schema） */
    List<Map<String, Object>> selectDbTableList(String tableName);

    /** 导入表结构到 gen_table */
    int importTables(String[] tableNames);

    List<GenTable> pageList(GenTable query, int pageNum, int pageSize);

    GenTable selectById(Long tableId);

    boolean update(GenTable table);

    int deleteByIds(Long[] tableIds);

    /** 在线预览生成代码：文件路径 -> 内容 */
    Map<String, String> preview(Long tableId);

    /** 批量生成代码并打包 Zip 字节 */
    byte[] batchGenCode(Long[] tableIds);
}
