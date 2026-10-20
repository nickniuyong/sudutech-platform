package com.sudutech.common.sql;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL 执行结果
 */
@Data
public class SqlResult {

    /** 列名（按查询顺序） */
    private List<String> columns;

    /** 数据行 */
    private List<Map<String, Object>> rows;

    /** 实际执行耗时ms */
    private long costTime;

    /** 是否追加了LIMIT */
    private boolean limitApplied;

    public Map<String, Object> toSummary() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("columns", columns);
        map.put("total", rows == null ? 0 : rows.size());
        map.put("costTime", costTime);
        return map;
    }
}
