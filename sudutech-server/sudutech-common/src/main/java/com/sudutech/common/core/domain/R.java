package com.sudutech.common.core.domain;

import com.sudutech.common.constant.HttpStatus;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一返回体 {code, msg, data}
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;
    private T data;

    public static final int CODE_SUCCESS = HttpStatus.SUCCESS;
    public static final int CODE_ERROR = HttpStatus.ERROR;

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return rest(CODE_SUCCESS, "操作成功", null);
    }

    public static <T> R<T> ok(T data) {
        return rest(CODE_SUCCESS, "操作成功", data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return rest(CODE_SUCCESS, msg, data);
    }

    public static <T> R<T> fail() {
        return rest(CODE_ERROR, "操作失败", null);
    }

    public static <T> R<T> fail(String msg) {
        return rest(CODE_ERROR, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return rest(code, msg, null);
    }

    public static <T> R<Map<String, Object>> ok(Map<String, Object> data) {
        return rest(CODE_SUCCESS, "操作成功", data);
    }

    private static <T> R<T> rest(int code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    @SuppressWarnings("unchecked")
    public static <T> R<T> okMap(Object... kv) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return (R<T>) ok(map);
    }
}
