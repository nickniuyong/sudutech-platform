package com.sudutech.report.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.ai.domain.AiDatasource;
import com.sudutech.ai.mapper.AiDatasourceMapper;
import com.sudutech.common.config.CryptoService;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.sql.DynamicDataSourceManager;
import com.sudutech.common.sql.SqlExecutor;
import com.sudutech.common.sql.SqlResult;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.report.domain.ReportChart;
import com.sudutech.report.domain.ReportDataset;
import com.sudutech.report.domain.ReportPage;
import com.sudutech.report.mapper.ReportChartMapper;
import com.sudutech.report.mapper.ReportDatasetMapper;
import com.sudutech.report.mapper.ReportPageMapper;
import com.sudutech.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static com.sudutech.report.domain.table.ReportChartTableDef.REPORT_CHART;
import static com.sudutech.report.domain.table.ReportDatasetTableDef.REPORT_DATASET;
import static com.sudutech.report.domain.table.ReportPageTableDef.REPORT_PAGE;

/**
 * 报表服务实现
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportPageMapper pageMapper;
    private final ReportDatasetMapper datasetMapper;
    private final ReportChartMapper chartMapper;
    private final AiDatasourceMapper datasourceMapper;
    private final SqlExecutor sqlExecutor;
    private final DynamicDataSourceManager dataSourceManager;
    private final CryptoService cryptoService;

    @Override
    public List<ReportPage> pageList(String pageName) {
        QueryWrapper wrapper = QueryWrapper.create().where(REPORT_PAGE.DEL_FLAG.eq("0"));
        if (StringUtils.isNotBlank(pageName)) {
            wrapper.and(REPORT_PAGE.PAGE_NAME.like(pageName));
        }
        wrapper.orderBy(REPORT_PAGE.PAGE_ID.desc());
        return pageMapper.selectListByQuery(wrapper);
    }

    @Override
    public ReportPage savePage(ReportPage page) {
        if (page.getPageId() == null) {
            pageMapper.insert(page);
        } else {
            page.setDelFlag(null);
            pageMapper.update(page);
        }
        return pageMapper.selectOneById(page.getPageId());
    }

    @Override
    public int deletePages(List<Long> pageIds) {
        return pageMapper.deleteBatchByIds(pageIds);
    }

    @Override
    public List<ReportDataset> datasetList(String name) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (StringUtils.isNotBlank(name)) {
            wrapper.and(REPORT_DATASET.DATASET_NAME.like(name));
        }
        wrapper.orderBy(REPORT_DATASET.DATASET_ID.desc());
        return datasetMapper.selectListByQuery(wrapper);
    }

    @Override
    public ReportDataset saveDataset(ReportDataset dataset) {
        if (dataset.getDatasetId() == null) {
            if (dataset.getRowLimit() == null) {
                dataset.setRowLimit(1000);
            }
            datasetMapper.insert(dataset);
        } else {
            datasetMapper.update(dataset);
        }
        return datasetMapper.selectOneById(dataset.getDatasetId());
    }

    @Override
    public int deleteDatasets(List<Long> datasetIds) {
        return datasetMapper.deleteBatchByIds(datasetIds);
    }

    @Override
    public SqlResult executeDataset(Long datasetId, Map<String, Object> params) {
        ReportDataset dataset = datasetMapper.selectOneById(datasetId);
        if (dataset == null) {
            throw new ServiceException("数据集不存在");
        }
        AiDatasource ds = datasourceMapper.selectOneById(dataset.getDatasourceId());
        if (ds == null) {
            throw new ServiceException("数据源不存在");
        }
        String password = cryptoService.decrypt(ds.getPassword());
        DataSource dataSource = dataSourceManager.getPool(dataset.getDatasourceId(), ds.getJdbcUrl(),
                ds.getUsername(), password, driverClass(ds.getDbType()));
        int limit = dataset.getRowLimit() != null && dataset.getRowLimit() > 0 ? dataset.getRowLimit() : 1000;
        return sqlExecutor.execute(dataSource, dataset.getSqlText(), params, limit);
    }

    private String driverClass(String dbType) {
        return switch (dbType == null ? "mysql" : dbType) {
            case "mysql" -> "com.mysql.cj.jdbc.Driver";
            case "pg" -> "org.postgresql.Driver";
            case "clickhouse" -> "com.clickhouse.jdbc.ClickHouseDriver";
            default -> throw new ServiceException("暂不支持的数据库类型: " + dbType);
        };
    }

    @Override
    public List<ReportChart> chartList(Long pageId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(REPORT_CHART.PAGE_ID.eq(pageId))
                .orderBy(REPORT_CHART.SORT_NUM.asc(), REPORT_CHART.CHART_ID.asc());
        return chartMapper.selectListByQuery(wrapper);
    }

    @Override
    public ReportChart saveChart(ReportChart chart) {
        if (chart.getChartId() == null) {
            chartMapper.insert(chart);
        } else {
            chartMapper.update(chart);
        }
        return chartMapper.selectOneById(chart.getChartId());
    }

    @Override
    public int deleteCharts(List<Long> chartIds) {
        return chartMapper.deleteBatchByIds(chartIds);
    }
}
