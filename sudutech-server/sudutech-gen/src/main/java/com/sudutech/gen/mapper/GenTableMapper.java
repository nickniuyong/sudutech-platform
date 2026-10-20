package com.sudutech.gen.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sudutech.gen.domain.GenTable;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 生成表 Mapper
 */
public interface GenTableMapper extends BaseMapper<GenTable> {

    @Delete("delete from gen_table_column where table_id = #{tableId}")
    int deleteColumnsByTableId(@Param("tableId") Long tableId);
}
