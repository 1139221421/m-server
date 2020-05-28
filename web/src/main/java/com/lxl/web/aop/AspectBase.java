package com.lxl.web.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

public class AspectBase {
    /**
     * 获取当前执行的方法
     *
     * @param joinPoint 连接点
     * @return 方法
     */
    protected Method currentMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        Signature sig = joinPoint.getSignature();
        MethodSignature msig = null;
        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        msig = (MethodSignature) sig;
        Object target = joinPoint.getTarget();
        return target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
    }
}
