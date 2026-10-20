package com.sudutech.common.xss;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局 XSS 过滤：对查询参数做转义（JSON 请求体由 Jackson 处理，不做转义以保留富文本）
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class XssFilter extends OncePerRequestFilter {

    /** 排除路径（BPMN XML 等富内容） */
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/api/v1/wf/definition"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (EXCLUDE_URLS.stream().anyMatch(uri::contains)) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(new XssHttpServletRequestWrapper(request), response);
    }

    static class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

        public XssHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> raw = super.getParameterMap();
            Map<String, String[]> cleaned = new LinkedHashMap<>();
            raw.forEach((key, values) -> {
                String[] nv = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    nv[i] = escape(values[i]);
                }
                cleaned.put(key, nv);
            });
            return cleaned;
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return value == null ? null : escape(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) {
                return null;
            }
            String[] nv = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                nv[i] = escape(values[i]);
            }
            return nv;
        }

        private String escape(String value) {
            if (value == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder(value.length());
            for (char c : value.toCharArray()) {
                switch (c) {
                    case '<' -> sb.append("&lt;");
                    case '>' -> sb.append("&gt;");
                    case '"' -> sb.append("&quot;");
                    case '\'' -> sb.append("&#39;");
                    default -> sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}
