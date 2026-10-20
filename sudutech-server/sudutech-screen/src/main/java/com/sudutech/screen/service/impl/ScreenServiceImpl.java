package com.sudutech.screen.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.sql.DynamicDataSourceManager;
import com.sudutech.common.sql.SqlExecutor;
import com.sudutech.common.sql.SqlResult;
import com.sudutech.common.utils.JsonUtils;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.screen.domain.ScreenComponent;
import com.sudutech.screen.domain.ScreenDataset;
import com.sudutech.screen.domain.ScreenPage;
import com.sudutech.screen.domain.ScreenProject;
import com.sudutech.screen.mapper.ScreenComponentMapper;
import com.sudutech.screen.mapper.ScreenDatasetMapper;
import com.sudutech.screen.mapper.ScreenPageMapper;
import com.sudutech.screen.mapper.ScreenProjectMapper;
import com.sudutech.screen.service.ScreenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.sudutech.screen.domain.table.ScreenDatasetTableDef.SCREEN_DATASET;
import static com.sudutech.screen.domain.table.ScreenProjectTableDef.SCREEN_PROJECT;

/**
 * 大屏服务实现
 */
@Service
@RequiredArgsConstructor
public class ScreenServiceImpl implements ScreenService {

    private final ScreenProjectMapper projectMapper;
    private final ScreenPageMapper pageMapper;
    private final ScreenDatasetMapper datasetMapper;
    private final ScreenComponentMapper componentMapper;
    private final com.sudutech.ai.mapper.AiDatasourceMapper datasourceMapper;
    private final SqlExecutor sqlExecutor;
    private final DynamicDataSourceManager dataSourceManager;
    private final com.sudutech.common.config.CryptoService cryptoService;

    @Override
    public List<ScreenProject> projectList(String projectName, String status) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SCREEN_PROJECT.DEL_FLAG.eq("0"));
        if (StringUtils.isNotBlank(projectName)) {
            wrapper.and(SCREEN_PROJECT.PROJECT_NAME.like(projectName));
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.and(SCREEN_PROJECT.STATUS.eq(status));
        }
        wrapper.orderBy(SCREEN_PROJECT.PROJECT_ID.desc());
        return projectMapper.selectListByQuery(wrapper);
    }

    @Override
    public ScreenProject selectProject(Long projectId) {
        return projectMapper.selectOneById(projectId);
    }

    @Override
    public ScreenProject insertProject(ScreenProject project) {
        project.setProjectId(null);
        projectMapper.insert(project);
        // 初始化主页面
        ScreenPage page = new ScreenPage();
        page.setProjectId(project.getProjectId());
        page.setPageName("主页面");
        page.setCanvasConfig("{\"width\":1920,\"height\":1080,\"background\":\"#0b1c33\",\"scaleMode\":\"fit\"}");
        page.setComponentTree("[]");
        pageMapper.insert(page);
        return project;
    }

    @Override
    public ScreenProject updateProject(ScreenProject project) {
        project.setDelFlag(null);
        projectMapper.update(project);
        return projectMapper.selectOneById(project.getProjectId());
    }

    @Override
    public int deleteProjects(List<Long> projectIds) {
        return projectMapper.deleteBatchByIds(projectIds);
    }

    @Override
    public ScreenProject publish(Long projectId) {
        ScreenProject project = projectMapper.selectOneById(projectId);
        if (project == null) {
            throw new ServiceException("大屏项目不存在");
        }
        if (StringUtils.isBlank(project.getPublishUrl())) {
            ScreenProject update = new ScreenProject();
            update.setProjectId(projectId);
            update.setPublishUrl(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
            update.setStatus("1");
            projectMapper.update(update);
            return projectMapper.selectOneById(projectId);
        }
        ScreenProject update = new ScreenProject();
        update.setProjectId(projectId);
        update.setStatus("1");
        projectMapper.update(update);
        return projectMapper.selectOneById(projectId);
    }

    @Override
    public ScreenPage selectPage(Long projectId) {
        return pageMapper.selectOneByCondition(
                com.sudutech.screen.domain.table.ScreenPageTableDef.SCREEN_PAGE.PROJECT_ID.eq(projectId));
    }

    @Override
    public ScreenPage savePage(ScreenPage page) {
        ScreenPage exist = pageMapper.selectOneById(page.getPageId());
        if (exist == null) {
            pageMapper.insert(page);
        } else {
            page.setProjectId(exist.getProjectId());
            pageMapper.update(page);
        }
        return pageMapper.selectOneById(page.getPageId());
    }

    @Override
    public List<ScreenComponent> componentList() {
        return componentMapper.selectListByCondition(
                com.sudutech.screen.domain.table.ScreenComponentTableDef.SCREEN_COMPONENT.STATUS.eq("0"));
    }

    @Override
    public List<ScreenDataset> datasetList(Long projectId) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (projectId != null) {
            wrapper.and(SCREEN_DATASET.PROJECT_ID.eq(projectId));
        }
        wrapper.orderBy(SCREEN_DATASET.DATASET_ID.desc());
        return datasetMapper.selectListByQuery(wrapper);
    }

    @Override
    public ScreenDataset saveDataset(ScreenDataset dataset) {
        if (dataset.getDatasourceId() == null || StringUtils.isBlank(dataset.getSqlText())) {
            throw new ServiceException("数据源与SQL不能为空");
        }
        if (dataset.getDatasetId() == null) {
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
    public SqlResult executeDataset(Long datasetId, Map<String, Object> params, int limit) {
        ScreenDataset dataset = datasetMapper.selectOneById(datasetId);
        if (dataset == null) {
            throw new ServiceException("数据集不存在");
        }
        return doExecute(dataset.getDatasourceId(), dataset.getSqlText(), params, limit);
    }

    private SqlResult doExecute(Long datasourceId, String sqlText, Map<String, Object> params, int limit) {
        com.sudutech.ai.domain.AiDatasource ds = datasourceMapper.selectOneById(datasourceId);
        if (ds == null) {
            throw new ServiceException("数据源不存在");
        }
        String password = cryptoService.decrypt(ds.getPassword());
        DataSource dataSource = dataSourceManager.getPool(datasourceId, ds.getJdbcUrl(),
                ds.getUsername(), password, driverClass(ds.getDbType()));
        return sqlExecutor.execute(dataSource, sqlText, params, limit > 0 ? limit : 1000);
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
    public Map<String, Object> viewByPublishUrl(String publishUrl) {
        ScreenProject project = projectMapper.selectOneByCondition(
                SCREEN_PROJECT.PUBLISH_URL.eq(publishUrl).and(SCREEN_PROJECT.STATUS.eq("1")));
        if (project == null) {
            throw new ServiceException("分享链接无效或大屏未发布");
        }
        ScreenPage page = selectPage(project.getProjectId());
        List<ScreenDataset> datasets = datasetList(project.getProjectId());

        // 服务端执行数据集并附带数据（发布页匿名访问，避免前端二次调用鉴权接口）
        List<Map<String, Object>> datasetWithRows = new java.util.ArrayList<>();
        for (ScreenDataset dataset : datasets) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("datasetId", dataset.getDatasetId());
            item.put("datasetName", dataset.getDatasetName());
            item.put("refreshInterval", dataset.getRefreshInterval());
            try {
                SqlResult result = executeDataset(dataset.getDatasetId(), Map.of(), 500);
                item.put("columns", result.getColumns());
                item.put("rows", result.getRows());
            } catch (Exception e) {
                item.put("columns", List.of());
                item.put("rows", List.of());
            }
            datasetWithRows.add(item);
        }

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("project", project);
        view.put("page", page);
        view.put("datasets", datasetWithRows);
        return view;
    }
}
