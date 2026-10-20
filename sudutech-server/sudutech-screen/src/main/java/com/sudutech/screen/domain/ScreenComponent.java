package com.sudutech.screen.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * 大屏组件注册表 screen_component
 */
@Data
@Table("screen_component")
public class ScreenComponent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long componentId;

    /** 前端注册 key */
    private String componentCode;

    private String componentName;

    /** chart/text/border */
    private String componentType;

    private String icon;

    /** 默认配置JSON */
    private String defaultConfig;

    private Integer defaultWidth;

    private Integer defaultHeight;

    private Integer orderNum;

    private String status;
}
