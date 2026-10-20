package com.sudutech.workflow.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.core.domain.LoginUser;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.LoginHelper;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.system.domain.SysUser;
import com.sudutech.system.service.SysUserService;
import com.sudutech.workflow.domain.WfBusiness;
import com.sudutech.workflow.domain.WfDefinitionExt;
import com.sudutech.workflow.domain.WfNodeConfig;
import com.sudutech.workflow.domain.WfOperationLog;
import com.sudutech.workflow.domain.WfTask;
import com.sudutech.workflow.mapper.WfBusinessMapper;
import com.sudutech.workflow.mapper.WfDefinitionExtMapper;
import com.sudutech.workflow.mapper.WfNodeConfigMapper;
import com.sudutech.workflow.mapper.WfOperationLogMapper;
import com.sudutech.workflow.mapper.WfTaskMapper;
import com.sudutech.workflow.service.WfInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sudutech.workflow.domain.table.WfBusinessTableDef.WF_BUSINESS;
import static com.sudutech.workflow.domain.table.WfTaskTableDef.WF_TASK;

/**
 * 流程实例服务实现：
 * 简化顺序审批引擎 —— 按 wf_node_config.node_order 顺序流转；
 * 审批人解析：USER 指定用户 / ROLE 角色下用户 / DEPT_LEADER 发起人部门负责人；
 * approve_mode=ANY 任一通过，ALL 会签（全部通过才流转到下一节点）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfInstanceServiceImpl implements WfInstanceService {

    private final WfBusinessMapper businessMapper;
    private final WfTaskMapper taskMapper;
    private final WfDefinitionExtMapper definitionMapper;
    private final WfNodeConfigMapper nodeConfigMapper;
    private final WfOperationLogMapper operationLogMapper;
    private final SysUserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfBusiness start(String processKey, String businessId, String businessTitle, String formData) {
        LoginUser loginUser = LoginHelper.getLoginUserRequired();
        WfDefinitionExt definition = definitionMapper.selectListByCondition(
                        com.sudutech.workflow.domain.table.WfDefinitionExtTableDef.WF_DEFINITION_EXT.PROCESS_KEY.eq(processKey)
                                .and(com.sudutech.workflow.domain.table.WfDefinitionExtTableDef.WF_DEFINITION_EXT.STATUS.eq("0")))
                .stream().findFirst()
                .orElseThrow(() -> new ServiceException("流程[" + processKey + "]不存在或未启用"));
        List<WfNodeConfig> nodes = selectOrderedNodes(definition.getId());
        if (nodes.isEmpty()) {
            throw new ServiceException("流程未配置审批节点，请先在流程定义中配置节点");
        }

        WfBusiness business = new WfBusiness();
        business.setProcessKey(processKey);
        business.setProcessInstanceId("SIM-" + UUID.randomUUID().toString().replace("-", ""));
        business.setBusinessId(businessId);
        business.setBusinessTitle(businessTitle == null ? "" : businessTitle);
        business.setStatus("RUNNING");
        business.setCurrentNode(nodes.get(0).getNodeKey());
        business.setApplicantUserId(loginUser.getUserId());
        business.setFormData(formData);
        business.setCreateTime(LocalDateTime.now());
        businessMapper.insert(business);

        createNodeTasks(business, definition, nodes.get(0));
        saveLog(business.getProcessInstanceId(), null, nodes.get(0).getNodeKey(),
                loginUser.getUserId(), "START", "发起申请");
        return business;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long taskId, String comment) {
        LoginUser loginUser = LoginHelper.getLoginUserRequired();
        WfTask task = requirePendingTask(taskId, loginUser.getUserId());
        WfBusiness business = businessMapper.selectOneById(task.getBusinessId());
        WfDefinitionExt definition = latestDefinition(business.getProcessKey());
        List<WfNodeConfig> nodes = selectOrderedNodes(definition.getId());
        WfNodeConfig current = nodes.stream()
                .filter(n -> n.getNodeKey().equals(task.getNodeKey())).findFirst()
                .orElseThrow(() -> new ServiceException("节点配置缺失"));

        // 完成当前任务
        task.setStatus("DONE");
        task.setResult("APPROVE");
        task.setComment(comment);
        task.setFinishTime(LocalDateTime.now());
        taskMapper.update(task);
        saveLog(business.getProcessInstanceId(), taskId, task.getNodeKey(),
                loginUser.getUserId(), "APPROVE", comment);

        // ALL 会签：还有未完成的本节点任务则等待
        long pendingSameNode = countPending(business.getId(), task.getNodeKey());
        if ("ALL".equals(current.getApproveMode()) && pendingSameNode > 0) {
            return;
        }

        // 流转到下一节点或结束
        int idx = nodes.indexOf(current);
        if (idx + 1 < nodes.size()) {
            WfNodeConfig next = nodes.get(idx + 1);
            business.setCurrentNode(next.getNodeKey());
            business.setUpdateTime(LocalDateTime.now());
            businessMapper.update(business);
            createNodeTasks(business, definition, next);
        } else {
            business.setStatus("APPROVED");
            business.setCurrentNode("");
            business.setUpdateTime(LocalDateTime.now());
            businessMapper.update(business);
            saveLog(business.getProcessInstanceId(), null, "", loginUser.getUserId(), "APPROVE", "流程审批通过");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long taskId, String comment) {
        LoginUser loginUser = LoginHelper.getLoginUserRequired();
        WfTask task = requirePendingTask(taskId, loginUser.getUserId());
        WfBusiness business = businessMapper.selectOneById(task.getBusinessId());

        task.setStatus("DONE");
        task.setResult("REJECT");
        task.setComment(comment);
        task.setFinishTime(LocalDateTime.now());
        taskMapper.update(task);

        // 取消该实例全部待办
        List<WfTask> pendings = taskMapper.selectListByCondition(
                WF_TASK.BUSINESS_ID.eq(business.getId()).and(WF_TASK.STATUS.eq("PENDING")));
        for (WfTask pending : pendings) {
            pending.setStatus("CANCELED");
            pending.setFinishTime(LocalDateTime.now());
            taskMapper.update(pending);
        }
        business.setStatus("REJECTED");
        business.setCurrentNode("");
        business.setUpdateTime(LocalDateTime.now());
        businessMapper.update(business);
        saveLog(business.getProcessInstanceId(), taskId, task.getNodeKey(),
                loginUser.getUserId(), "REJECT", comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegate(Long taskId, Long targetUserId, String comment) {
        LoginUser loginUser = LoginHelper.getLoginUserRequired();
        WfTask task = requirePendingTask(taskId, loginUser.getUserId());
        task.setAssigneeId(targetUserId);
        taskMapper.update(task);
        saveLog(task.getProcessInstanceId(), taskId, task.getNodeKey(),
                loginUser.getUserId(), "DELEGATE", "委派给用户[" + targetUserId + "] " + (comment == null ? "" : comment));
    }

    @Override
    public Page<WfTask> todoTasks(int pageNum, int pageSize) {
        LoginUser loginUser = LoginHelper.getLoginUserRequired();
        Page<WfTask> page = taskMapper.paginate(Page.of(pageNum, pageSize),
                QueryWrapper.create()
                        .where(WF_TASK.ASSIGNEE_ID.eq(loginUser.getUserId()).and(WF_TASK.STATUS.eq("PENDING")))
                        .orderBy(WF_TASK.ID.desc()));
        fillTaskInfo(page.getRecords());
        return page;
    }

    @Override
    public Page<WfTask> doneTasks(int pageNum, int pageSize) {
        LoginUser loginUser = LoginHelper.getLoginUserRequired();
        Page<WfTask> page = taskMapper.paginate(Page.of(pageNum, pageSize),
                QueryWrapper.create()
                        .where(WF_TASK.ASSIGNEE_ID.eq(loginUser.getUserId()).and(WF_TASK.STATUS.eq("DONE")))
                        .orderBy(WF_TASK.FINISH_TIME.desc()));
        fillTaskInfo(page.getRecords());
        return page;
    }

    @Override
    public Page<WfBusiness> myInstances(int pageNum, int pageSize) {
        LoginUser loginUser = LoginHelper.getLoginUserRequired();
        Page<WfBusiness> page = businessMapper.paginate(Page.of(pageNum, pageSize),
                QueryWrapper.create()
                        .where(WF_BUSINESS.APPLICANT_USER_ID.eq(loginUser.getUserId()))
                        .orderBy(WF_BUSINESS.ID.desc()));
        page.getRecords().forEach(this::fillBusinessInfo);
        return page;
    }

    @Override
    public List<WfOperationLog> trace(String processInstanceId) {
        return operationLogMapper.selectListByCondition(
                        com.sudutech.workflow.domain.table.WfOperationLogTableDef.WF_OPERATION_LOG.PROCESS_INSTANCE_ID.eq(processInstanceId))
                .stream().sorted(java.util.Comparator.comparing(WfOperationLog::getId)).toList();
    }

    @Override
    public List<WfTask> instanceTasks(String processInstanceId) {
        List<WfTask> tasks = taskMapper.selectListByCondition(WF_TASK.PROCESS_INSTANCE_ID.eq(processInstanceId));
        fillTaskInfo(tasks);
        return tasks;
    }

    @Override
    public WfBusiness selectInstance(Long businessId) {
        WfBusiness business = businessMapper.selectOneById(businessId);
        if (business != null) {
            fillBusinessInfo(business);
        }
        return business;
    }

    // ---------------- private ----------------

    private void createNodeTasks(WfBusiness business, WfDefinitionExt definition, WfNodeConfig node) {
        List<Long> assignees = resolveAssignees(node, business.getApplicantUserId());
        if (assignees.isEmpty()) {
            log.warn("节点[{}]未解析到审批人，流程实例[{}]将停在该节点", node.getNodeKey(), business.getProcessInstanceId());
            return;
        }
        for (Long userId : assignees) {
            WfTask task = new WfTask();
            task.setProcessInstanceId(business.getProcessInstanceId());
            task.setBusinessId(business.getId());
            task.setNodeKey(node.getNodeKey());
            task.setNodeName(node.getNodeName());
            task.setAssigneeId(userId);
            task.setStatus("PENDING");
            task.setCreateTime(LocalDateTime.now());
            taskMapper.insert(task);
        }
    }

    private List<Long> resolveAssignees(WfNodeConfig node, Long applicantUserId) {
        List<Long> userIds = new ArrayList<>();
        String value = node.getAssigneeValue();
        if (StringUtils.isBlank(node.getAssigneeType()) || StringUtils.isBlank(value)) {
            return userIds;
        }
        switch (node.getAssigneeType()) {
            case "USER" -> {
                for (String id : value.split(",")) {
                    try {
                        userIds.add(Long.parseLong(id.trim()));
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
            case "ROLE" -> userService.selectUsersByRoleKey(value)
                    .forEach(u -> userIds.add(u.getUserId()));
            case "DEPT_LEADER" -> {
                SysUser applicant = userService.selectById(applicantUserId);
                if (applicant != null) {
                    // 部门负责人：leader_user_id 存于部门表；演示回退为申请人本人
                    userIds.add(applicant.getUserId());
                }
            }
            default -> log.warn("暂不支持的审批人类型: {}", node.getAssigneeType());
        }
        return userIds;
    }

    private WfTask requirePendingTask(Long taskId, Long userId) {
        WfTask task = taskMapper.selectOneById(taskId);
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new ServiceException("任务已处理");
        }
        if (!task.getAssigneeId().equals(userId)) {
            throw new ServiceException("无权处理该任务");
        }
        return task;
    }

    private long countPending(Long businessId, String nodeKey) {
        return taskMapper.selectCountByCondition(
                WF_TASK.BUSINESS_ID.eq(businessId).and(WF_TASK.NODE_KEY.eq(nodeKey))
                        .and(WF_TASK.STATUS.eq("PENDING")));
    }

    private WfDefinitionExt latestDefinition(String processKey) {
        return definitionMapper.selectListByCondition(
                        com.sudutech.workflow.domain.table.WfDefinitionExtTableDef.WF_DEFINITION_EXT.PROCESS_KEY.eq(processKey))
                .stream().max(java.util.Comparator.comparingInt(d -> d.getVersion() == null ? 0 : d.getVersion()))
                .orElseThrow(() -> new ServiceException("流程定义不存在"));
    }

    private List<WfNodeConfig> selectOrderedNodes(Long definitionId) {
        return nodeConfigMapper.selectListByCondition(
                        com.sudutech.workflow.domain.table.WfNodeConfigTableDef.WF_NODE_CONFIG.DEFINITION_ID.eq(definitionId))
                .stream().sorted(java.util.Comparator.comparing(
                        c -> c.getNodeOrder() == null ? 0 : c.getNodeOrder()))
                .toList();
    }

    private void saveLog(String instanceId, Long taskId, String nodeKey, Long userId, String operation, String opinion) {
        WfOperationLog logRow = new WfOperationLog();
        logRow.setProcessInstanceId(instanceId);
        logRow.setTaskId(taskId);
        logRow.setNodeKey(nodeKey);
        logRow.setOperatorUserId(userId);
        logRow.setOperation(operation);
        logRow.setOpinion(opinion);
        logRow.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(logRow);
    }

    private void fillTaskInfo(List<WfTask> tasks) {
        for (WfTask task : tasks) {
            if (task.getAssigneeId() != null) {
                SysUser user = userService.selectById(task.getAssigneeId());
                task.setAssigneeName(user != null ? user.getNickName() : "");
            }
            WfBusiness business = businessMapper.selectOneById(task.getBusinessId());
            if (business != null) {
                task.setBusinessTitle(business.getBusinessTitle());
                SysUser applicant = userService.selectById(business.getApplicantUserId());
                task.setApplicantName(applicant != null ? applicant.getNickName() : "");
            }
        }
    }

    private void fillBusinessInfo(WfBusiness business) {
        SysUser applicant = userService.selectById(business.getApplicantUserId());
        business.setApplicantName(applicant != null ? applicant.getNickName() : "");
        WfDefinitionExt def = definitionMapper.selectOneByCondition(
                com.sudutech.workflow.domain.table.WfDefinitionExtTableDef.WF_DEFINITION_EXT.PROCESS_KEY.eq(business.getProcessKey()));
        if (def != null) {
            business.setProcessName(def.getProcessName());
        }
    }
}
