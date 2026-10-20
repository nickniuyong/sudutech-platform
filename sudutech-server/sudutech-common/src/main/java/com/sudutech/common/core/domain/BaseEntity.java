package com.sudutech.common.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import com.sudutech.common.utils.LoginHelper;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 实体基类：审计字段 + 自动填充
 */
@Data
public abstract class BaseEntity implements Serializable, InsertListener, UpdateListener {

    private static final long serialVersionUID = 1L;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

    /** 查询透传参数（不在表中） */
    @JsonIgnore
    private transient Map<String, Object> params = new HashMap<>();

    @Override
    public void onInsert(Object entity) {
        LocalDateTime now = LocalDateTime.now();
        if (entity instanceof BaseEntity base) {
            if (base.getCreateTime() == null) {
                base.setCreateTime(now);
            }
            if (base.getCreateBy() == null || base.getCreateBy().isEmpty()) {
                base.setCreateBy(currentUserName());
            }
            base.setUpdateTime(now);
            base.setUpdateBy(currentUserName());
        }
    }

    @Override
    public void onUpdate(Object entity) {
        if (entity instanceof BaseEntity base) {
            base.setUpdateTime(LocalDateTime.now());
            base.setUpdateBy(currentUserName());
        }
    }

    private String currentUserName() {
        try {
            LoginUser user = LoginHelper.getLoginUser();
            return user != null ? user.getUserName() : "system";
        } catch (Exception e) {
            return "system";
        }
    }
}
