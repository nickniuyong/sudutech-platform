package com.sudutech.system.quartz;

/**
 * 调度常量
 */
public class ScheduleConstants {

    public static final String JOB_DATA_KEY = "sysJob";

    /** 立即执行 */
    public static final String MISFIRE_NOW = "1";
    /** 执行一次 */
    public static final String MISFIRE_ONCE = "2";
    /** 放弃执行 */
    public static final String MISFIRE_IGNORE = "3";

    /** 允许并发 */
    public static final String CONCURRENT_ENABLE = "0";
    /** 禁止并发 */
    public static final String CONCURRENT_DISABLE = "1";

    private ScheduleConstants() {
    }
}
