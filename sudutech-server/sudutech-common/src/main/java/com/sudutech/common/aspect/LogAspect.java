package com.sudutech.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudutech.common.annotation.Log;
import com.sudutech.common.core.service.OperLogRecorder;
import com.sudutech.common.utils.ServletUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired(required = false)
    private OperLogRecorder operLogRecorder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(logAnno)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnno) throws Throwable {
        long start = System.currentTimeMillis();
        Throwable error = null;
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            try {
                recordLog(joinPoint, logAnno, result, error, System.currentTimeMillis() - start);
            } catch (Exception ex) {
                log.warn("记录操作日志失败", ex);
            }
        }
    }

    private void recordLog(ProceedingJoinPoint joinPoint, Log logAnno, Object result,
                           Throwable error, long costTime) {
        if (operLogRecorder == null) {
            return;
        }
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        String method = className + "." + methodName + "()";

        HttpServletRequest request = ServletUtils.getRequest();
        String requestMethod = request != null ? request.getMethod() : "";
        String operUrl = request != null ? request.getRequestURI() : "";

        String operParam = logAnno.saveParam() ? jsonOf(argsToMap(joinPoint.getArgs())) : "";
        String jsonResult = error == null ? jsonOf(result) : "";

        operLogRecorder.record(logAnno.title(), logAnno.businessType().ordinal(), method,
                requestMethod, operUrl, operParam, jsonResult,
                error == null ? 0 : 1, error == null ? "" : String.valueOf(error.getMessage()),
                costTime);
    }

    /** 过滤掉 Servlet/流等不可序列化参数 */
    private Map<String, Object> argsToMap(Object[] args) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (args == null) {
            return map;
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile || arg instanceof byte[]) {
                continue;
            }
            map.put("arg" + i, arg);
        }
        return map;
    }

    private String jsonOf(Object obj) {
        if (obj == null) {
            return "";
        }
        try {
            String json = objectMapper.writeValueAsString(obj);
            return json.length() > 2000 ? json.substring(0, 2000) : json;
        } catch (Exception e) {
            return "";
        }
    }
}
