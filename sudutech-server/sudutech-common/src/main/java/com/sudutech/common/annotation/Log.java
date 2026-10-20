package com.sudutech.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解：标注在 Controller 方法上，由 LogAspect 自动记录入参出参
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** 模块名称 */
    String title() default "";

    /** 业务类型 */
    BusinessType businessType() default BusinessType.OTHER;

    /** 是否保存请求参数 */
    boolean saveParam() default true;
}
