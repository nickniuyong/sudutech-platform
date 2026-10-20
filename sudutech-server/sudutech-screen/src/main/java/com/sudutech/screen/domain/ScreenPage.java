package com.sudutech.screen.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * 大屏页面表 screen_page
 */
@Data
@Table("screen_page")
public class ScreenPage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long pageId;

    private Long projectId;

    private String pageName;

    /** 画布配置JSON 宽高/背景/缩放 */
    private String canvasConfig;

    /** 组件树JSON */
    private String componentTree;
}
