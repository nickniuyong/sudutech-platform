package com.sudutech.common.core.page;

import com.mybatisflex.core.paginate.Page;
import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageQuery {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    /** 排序字段（数据库列名，可选） */
    private String orderByColumn;
    /** 排序方向 asc/desc */
    private String isAsc;

    public <T> Page<T> toPage() {
        return Page.of(pageNum == null || pageNum < 1 ? 1 : pageNum,
                pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 500));
    }
}
