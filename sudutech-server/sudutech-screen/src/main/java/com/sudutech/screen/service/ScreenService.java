package com.sudutech.screen.service;

import com.sudutech.common.sql.SqlResult;
import com.sudutech.screen.domain.ScreenDataset;
import com.sudutech.screen.domain.ScreenPage;
import com.sudutech.screen.domain.ScreenProject;

import java.util.List;
import java.util.Map;

/**
 * 大屏服务
 */
public interface ScreenService {

    List<ScreenProject> projectList(String projectName, String status);

    ScreenProject selectProject(Long projectId);

    ScreenProject insertProject(ScreenProject project);

    ScreenProject updateProject(ScreenProject project);

    int deleteProjects(List<Long> projectIds);

    /** 发布：生成分享标识 */
    ScreenProject publish(Long projectId);

    /** 读取画布 */
    ScreenPage selectPage(Long projectId);

    /** 保存画布 */
    ScreenPage savePage(ScreenPage page);

    /** 组件库 */
    List<com.sudutech.screen.domain.ScreenComponent> componentList();

    /** 数据集 CRUD */
    List<ScreenDataset> datasetList(Long projectId);

    ScreenDataset saveDataset(ScreenDataset dataset);

    int deleteDatasets(List<Long> datasetIds);

    /** 执行数据集预览 */
    SqlResult executeDataset(Long datasetId, Map<String, Object> params, int limit);

    /** 发布态：按分享标识读取项目+画布+数据集配置（匿名） */
    Map<String, Object> viewByPublishUrl(String publishUrl);
}
