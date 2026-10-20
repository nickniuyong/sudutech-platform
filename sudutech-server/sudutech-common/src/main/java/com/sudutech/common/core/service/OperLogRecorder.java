package com.sudutech.common.core.service;

/**
 * 操作日志落库回调接口。
 * common 模块不依赖具体业务表，由 system 模块提供实现。
 */
public interface OperLogRecorder {

    /**
     * @param title          模块标题
     * @param businessType   业务类型序数（对应 BusinessType）
     * @param method         类名.方法名
     * @param requestMethod  HTTP 方法
     * @param operUrl        请求URL
     * @param operParam      入参JSON
     * @param jsonResult     出参JSON
     * @param status         0正常 1异常
     * @param errorMsg       异常消息
     * @param costTime       耗时ms
     */
    void record(String title, int businessType, String method, String requestMethod,
                String operUrl, String operParam, String jsonResult,
                int status, String errorMsg, long costTime);
}
