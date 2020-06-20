package com.lxl.web.aop;

import com.lxl.common.enums.LogTypeEnum;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.annotations.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @description: 日志Aop
 */
@Aspect
@Component
public class LogAspect extends AspectBase {

    /**
     * 日志切入点
     */
    @Resource
    private LogPoint logPoint;


    /**
     * 保存系统操作日志
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 调用出错
     */
    @Around(value = "@annotation(com.lxl.web.annotations.Log)")
    public Object doSaveLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 解析Log注解
        Method currentMethod = currentMethod(joinPoint);
        ResponseInfo responseInfo = (ResponseInfo) joinPoint.proceed();
        Log log = currentMethod.getAnnotation(Log.class);
        LogTypeEnum value = log.value();
        if (logPoint != null) {
            logPoint.saveLog(joinPoint, value, responseInfo);
        }
        return responseInfo;
    }


    public LogPoint getLogPoint() {
        return logPoint;
    }

    public void setLogPoint(LogPoint logPoint) {
        this.logPoint = logPoint;
    }

}
