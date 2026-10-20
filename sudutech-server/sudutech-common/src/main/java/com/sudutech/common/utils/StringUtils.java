package com.sudutech.common.utils;

/**
 * 字符串工具
 */
public class StringUtils extends org.springframework.util.StringUtils {

    public static boolean isNotBlank(String str) {
        return hasText(str);
    }

    public static boolean isBlank(String str) {
        return !hasText(str);
    }

    public static boolean isEmpty(Object obj) {
        return obj == null || !hasText(String.valueOf(obj));
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /** 下划线转驼峰 user_name -> userName */
    public static String toCamelCase(String str) {
        if (!hasText(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                upperNext = true;
            } else if (upperNext) {
                sb.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 驼峰转下划线 userName -> user_name */
    public static String toUnderScoreCase(String str) {
        if (!hasText(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 首字母大写 */
    public static String capitalize(String str) {
        if (!hasText(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /** 首字母小写 */
    public static String uncapitalize(String str) {
        if (!hasText(str)) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /** 字符串数组转 IN 子句格式: 'a','b' */
    public static String joinInSql(String[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String v : values) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("'").append(v.replace("'", "''")).append("'");
        }
        return sb.toString();
    }
}
