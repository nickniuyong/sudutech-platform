package com.sudutech.workflow.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.workflow.domain.WfForm;
import com.sudutech.workflow.mapper.WfFormMapper;
import com.sudutech.workflow.service.WfFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sudutech.workflow.domain.table.WfFormTableDef.WF_FORM;

/**
 * 动态表单服务实现
 */
@Service
@RequiredArgsConstructor
public class WfFormServiceImpl implements WfFormService {

    private final WfFormMapper formMapper;

    @Override
    public List<WfForm> selectList(String formName) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (StringUtils.isNotBlank(formName)) {
            wrapper.and(WF_FORM.FORM_NAME.like(formName));
        }
        wrapper.orderBy(WF_FORM.ID.desc());
        return formMapper.selectListByQuery(wrapper);
    }

    @Override
    public WfForm selectById(Long id) {
        return formMapper.selectOneById(id);
    }

    @Override
    public void save(WfForm form) {
        if (StringUtils.isBlank(form.getFormKey())) {
            throw new ServiceException("表单key不能为空");
        }
        WfForm exist = formMapper.selectOneByCondition(WF_FORM.FORM_KEY.eq(form.getFormKey()));
        if (form.getId() == null) {
            if (exist != null) {
                throw new ServiceException("表单key已存在: " + form.getFormKey());
            }
            formMapper.insert(form);
        } else {
            if (exist != null && !exist.getId().equals(form.getId())) {
                throw new ServiceException("表单key已存在: " + form.getFormKey());
            }
            formMapper.update(form);
        }
    }

    @Override
    public int deleteById(Long id) {
        return formMapper.deleteById(id);
    }
}
