package com.sudutech.system.util;

import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务调用工具：解析 invokeTarget（beanName.method(arg1,arg2)）并反射调用
 * 安全约束：仅允许调用 Spring 容器内受管 Bean 的公开方法
 */
@Component
public class JobInvokeUtil {

    @Autowired
    private ApplicationContext applicationContext;

    public void invoke(String invokeTarget) {
        String beanName;
        String methodName;
        String methodArgs;
        int lparen = invokeTarget.indexOf("(");
        int dot = invokeTarget.lastIndexOf(".", lparen > 0 ? lparen : invokeTarget.length());
        if (dot < 0 || (lparen > 0 && lparen < dot)) {
            throw new ServiceException("任务调用目标格式错误: " + invokeTarget + "，应为 beanName.method(args)");
        }
        beanName = invokeTarget.substring(0, dot);
        if (lparen > 0) {
            methodName = invokeTarget.substring(dot + 1, lparen);
            int rparen = invokeTarget.lastIndexOf(")");
            methodArgs = rparen > lparen ? invokeTarget.substring(lparen + 1, rparen) : "";
        } else {
            methodName = invokeTarget.substring(dot + 1);
            methodArgs = "";
        }

        Object bean;
        try {
            bean = applicationContext.getBean(beanName);
        } catch (Exception e) {
            throw new ServiceException("任务Bean不存在: " + beanName);
        }

        List<Object> argValues = new ArrayList<>();
        List<Class<?>> argTypes = new ArrayList<>();
        if (StringUtils.isNotBlank(methodArgs)) {
            for (String arg : methodArgs.split(",")) {
                String trimmed = arg.trim();
                if (trimmed.startsWith("'") && trimmed.endsWith("'")) {
                    argValues.add(trimmed.substring(1, trimmed.length() - 1));
                    argTypes.add(String.class);
                } else if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
                    argValues.add(Boolean.parseBoolean(trimmed));
                    argTypes.add(Boolean.class);
                } else if (trimmed.matches("-?\\d+")) {
                    if (trimmed.matches("-?\\d{1,9}")) {
                        argValues.add(Integer.parseInt(trimmed));
                        argTypes.add(Integer.class);
                    } else {
                        argValues.add(Long.parseLong(trimmed));
                        argTypes.add(Long.class);
                    }
                } else {
                    argValues.add(trimmed);
                    argTypes.add(String.class);
                }
            }
        }

        try {
            Method method = bean.getClass().getMethod(methodName, argTypes.toArray(new Class[0]));
            method.invoke(bean, argValues.toArray());
        } catch (NoSuchMethodException e) {
            throw new ServiceException("任务方法不存在: " + beanName + "." + methodName);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new ServiceException("任务执行失败: " + e.getMessage());
        }
    }
}
