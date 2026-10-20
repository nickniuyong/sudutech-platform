package com.sudutech.report.service;

import com.sudutech.common.sql.SqlResult;
import com.sudutech.report.domain.ReportChart;
import com.sudutech.report.domain.ReportDataset;
import com.sudutech.report.domain.ReportPage;

import java.util.List;
import java.util.Map;

/**
 * 报表服务
 */
public interface ReportService {

    List<ReportPage> pageList(String pageName);

    ReportPage savePage(ReportPage page);

    int deletePages(List<Long> pageIds);

    List<ReportDataset> datasetList(String name);

    ReportDataset saveDataset(ReportDataset dataset);

    int deleteDatasets(List<Long> datasetIds);

    /** 执行数据集（SELECT-only + 强制LIMIT + 参数绑定） */
    SqlResult executeDataset(Long datasetId, Map<String, Object> params);

    List<ReportChart> chartList(Long pageId);

    ReportChart saveChart(ReportChart chart);

    int deleteCharts(List<Long> chartIds);
}
