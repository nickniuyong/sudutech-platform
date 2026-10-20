package com.sudutech.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志表 sys_login_log
 */
@Data
@Table("sys_login_log")
public class SysLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long infoId;

    private String userName;

    private String ipaddr;

    private String loginLocation;

    private String browser;

    private String os;

    /** 0成功 1失败 */
    private String status;

    private String msg;

    private LocalDateTime loginTime;
}
