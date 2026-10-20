package com.sudutech.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.sudutech.common.constant.HttpStatus;
import com.sudutech.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.warn("业务异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLoginException(NotLoginException e) {
        return R.fail(HttpStatus.UNAUTHORIZED, "认证失败，无法访问系统资源");
    }

    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public R<Void> handleNotPermissionException(Exception e) {
        return R.fail(HttpStatus.FORBIDDEN, "没有权限访问该接口");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.fail(HttpStatus.BAD_REQUEST, "不支持的请求方法: " + e.getMethod());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public R<Void> handleValidException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst().orElse("参数校验失败");
        return R.fail(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return R.fail(HttpStatus.BAD_REQUEST, "请求体格式错误");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public R<Void> handleNoResource(NoResourceFoundException e) {
        return R.fail(HttpStatus.NOT_FOUND, "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}]", request.getRequestURI(), e);
        return R.fail(HttpStatus.ERROR, "系统繁忙，请稍后再试");
    }
}
