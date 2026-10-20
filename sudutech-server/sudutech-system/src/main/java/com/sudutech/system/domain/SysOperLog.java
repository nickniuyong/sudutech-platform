package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志表 sys_oper_log
 */
@Data
@Table("sys_oper_log")
public class SysOperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long operId;

    private String title;

    private Integer businessType;

    private String method;

    private String requestMethod;

    private Integer operatorType;

    private String operName;

    private String operUrl;

    private String operIp;

    private String operParam;

    private String jsonResult;

    private Integer status;

    private String errorMsg;

    private Long costTime;

    private LocalDateTime operTime;
}
