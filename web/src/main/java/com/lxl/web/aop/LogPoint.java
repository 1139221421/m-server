package com.lxl.web.aop;

import com.lxl.common.entity.log.Log;
import com.lxl.common.enums.LogTypeEnum;
import com.lxl.web.mq.RocketMqConsumer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public interface LogPoint {

    public static final Logger LOGGER = LoggerFactory.getLogger(LogPoint.class);

    /**
     * 保存日志
     *
     * @param joinPoint
     * @param logTypeEnum
     */
    void saveLog(ProceedingJoinPoint joinPoint, LogTypeEnum logTypeEnum);

    /**
     * 生成操作日志
     *
     * @param joinPoint
     * @param logTypeEnum
     * @param request
     * @return
     */
    Log operateLog(ProceedingJoinPoint joinPoint, LogTypeEnum logTypeEnum, HttpServletRequest request);
}
