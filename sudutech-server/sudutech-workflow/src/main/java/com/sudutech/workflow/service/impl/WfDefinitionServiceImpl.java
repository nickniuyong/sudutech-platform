package com.sudutech.workflow.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.workflow.domain.WfDefinitionExt;
import com.sudutech.workflow.domain.WfNodeConfig;
import com.sudutech.workflow.mapper.WfDefinitionExtMapper;
import com.sudutech.workflow.mapper.WfNodeConfigMapper;
import com.sudutech.workflow.service.WfDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.sudutech.workflow.domain.table.WfDefinitionExtTableDef.WF_DEFINITION_EXT;
import static com.sudutech.workflow.domain.table.WfNodeConfigTableDef.WF_NODE_CONFIG;

/**
 * 流程定义服务实现
 */
@Service
@RequiredArgsConstructor
public class WfDefinitionServiceImpl implements WfDefinitionService {

    private final WfDefinitionExtMapper definitionMapper;
    private final WfNodeConfigMapper nodeConfigMapper;

    @Override
    public List<WfDefinitionExt> selectList(String processName, String status) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (StringUtils.isNotBlank(processName)) {
            wrapper.and(WF_DEFINITION_EXT.PROCESS_NAME.like(processName)
                    .or(WF_DEFINITION_EXT.PROCESS_KEY.like(processName)));
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.and(WF_DEFINITION_EXT.STATUS.eq(status));
        }
        wrapper.orderBy(WF_DEFINITION_EXT.ID.desc());
        List<WfDefinitionExt> list = definitionMapper.selectListByQuery(wrapper);
        list.forEach(d -> d.setNodeCount(nodeConfigMapper.selectCountByCondition(
                WF_NODE_CONFIG.DEFINITION_ID.eq(d.getId()))));
        return list;
    }

    @Override
    public WfDefinitionExt selectById(Long id) {
        return definitionMapper.selectOneById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfDefinitionExt deploy(WfDefinitionExt definition) {
        if (StringUtils.isBlank(definition.getProcessKey())) {
            throw new ServiceException("流程key不能为空");
        }
        if (StringUtils.isBlank(definition.getBpmnXml())) {
            throw new ServiceException("BPMN XML内容不能为空");
        }
        validateBpmnXml(definition.getBpmnXml());

        // 同一 processKey 版本递增：新版本记录，旧版本自动停用
        WfDefinitionExt latest = definitionMapper.selectListByCondition(
                        WF_DEFINITION_EXT.PROCESS_KEY.eq(definition.getProcessKey()))
                .stream().max(java.util.Comparator.comparingInt(d -> d.getVersion() == null ? 0 : d.getVersion()))
                .orElse(null);
        definition.setVersion(latest == null ? 1 : latest.getVersion() + 1);
        definition.setDeploymentId("DEP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        definition.setDeployStatus("1");
        if (latest != null) {
            WfDefinitionExt disable = new WfDefinitionExt();
            disable.setId(latest.getId());
            disable.setStatus("1");
            definitionMapper.update(disable);
        }
        definition.setId(null);
        definition.setStatus("0");
        definitionMapper.insert(definition);
        return definition;
    }

    /** 基础 BPMN 结构校验（definitions/process/candidate 节点） */
    private void validateBpmnXml(String xml) {
        // 兼容带命名空间前缀的 bpmn-js 输出（如 <bpmn2:definitions>）
        if (!xml.contains("definitions") || !xml.contains("process")) {
            throw new ServiceException("无效的BPMN 2.0 XML：缺少 definitions 或 process 元素");
        }
    }

    @Override
    public String selectXml(Long id) {
        WfDefinitionExt definition = definitionMapper.selectOneById(id);
        if (definition == null) {
            throw new ServiceException("流程定义不存在");
        }
        return definition.getBpmnXml();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        nodeConfigMapper.deleteByCondition(WF_NODE_CONFIG.DEFINITION_ID.eq(id));
        return definitionMapper.deleteById(id);
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        WfDefinitionExt update = new WfDefinitionExt();
        update.setId(id);
        update.setStatus(status);
        return definitionMapper.update(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNodeConfigs(Long definitionId, List<WfNodeConfig> configs) {
        if (definitionMapper.selectOneById(definitionId) == null) {
            throw new ServiceException("流程定义不存在");
        }
        nodeConfigMapper.deleteByCondition(WF_NODE_CONFIG.DEFINITION_ID.eq(definitionId));
        if (configs != null) {
            int order = 1;
            for (WfNodeConfig config : configs) {
                config.setId(null);
                config.setDefinitionId(definitionId);
                if (config.getNodeOrder() == null) {
                    config.setNodeOrder(order);
                }
                nodeConfigMapper.insert(config);
                order++;
            }
        }
    }

    @Override
    public List<WfNodeConfig> selectNodeConfigs(Long definitionId) {
        return nodeConfigMapper.selectListByCondition(WF_NODE_CONFIG.DEFINITION_ID.eq(definitionId))
                .stream().sorted(java.util.Comparator.comparing(
                        c -> c.getNodeOrder() == null ? 0 : c.getNodeOrder()))
                .toList();
    }
}
