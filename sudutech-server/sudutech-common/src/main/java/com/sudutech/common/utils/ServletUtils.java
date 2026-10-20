package com.sudutech.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Servlet 工具
 */
public class ServletUtils {

    public static HttpServletRequest getRequest() {
        return servletAttributes().map(ServletRequestAttributes::getRequest).orElse(null);
    }

    public static HttpServletResponse getResponse() {
        return servletAttributes().map(ServletRequestAttributes::getResponse).orElse(null);
    }

    private static Optional<ServletRequestAttributes> servletAttributes() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(attrs -> attrs instanceof ServletRequestAttributes)
                .map(attrs -> (ServletRequestAttributes) attrs);
    }

    /** 获取客户端IP（支持代理头） */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (isUnknown(ip) && request.getHeader("X-Real-IP") != null) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    private static boolean isUnknown(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip);
    }

    private ServletUtils() {
    }
}
