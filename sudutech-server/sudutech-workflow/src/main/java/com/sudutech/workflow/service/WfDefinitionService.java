package com.sudutech.workflow.service;

import com.sudutech.workflow.domain.WfDefinitionExt;
import com.sudutech.workflow.domain.WfNodeConfig;

import java.util.List;

/**
 * 流程定义服务
 */
public interface WfDefinitionService {

    List<WfDefinitionExt> selectList(String processName, String status);

    WfDefinitionExt selectById(Long id);

    /** 保存并部署：校验 BPMN XML、写入部署记录 */
    WfDefinitionExt deploy(WfDefinitionExt definition);

    String selectXml(Long id);

    int deleteById(Long id);

    boolean updateStatus(Long id, String status);

    /** 保存节点配置（整批替换） */
    void saveNodeConfigs(Long definitionId, List<WfNodeConfig> configs);

    List<WfNodeConfig> selectNodeConfigs(Long definitionId);
}
