package com.sudutech.workflow.service;

import com.sudutech.workflow.domain.WfForm;

import java.util.List;

/**
 * 动态表单服务
 */
public interface WfFormService {

    List<WfForm> selectList(String formName);

    WfForm selectById(Long id);

    void save(WfForm form);

    int deleteById(Long id);
}
