package com.sudutech.common.core.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结构 {total, rows}
 */
@Data
public class TableDataInfo<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;
    private List<T> rows;
    private int code;
    private String msg;

    public TableDataInfo() {
        this.code = 200;
        this.msg = "查询成功";
    }

    public TableDataInfo(long total, List<T> rows) {
        this();
        this.total = total;
        this.rows = rows;
    }

    public static <T> TableDataInfo<T> build(long total, List<T> rows) {
        return new TableDataInfo<>(total, rows);
    }
}
