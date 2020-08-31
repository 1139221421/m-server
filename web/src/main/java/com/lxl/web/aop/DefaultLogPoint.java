package com.lxl.web.aop;

import com.alibaba.fastjson.JSON;
import com.lxl.common.entity.log.Log;
import com.lxl.common.enums.LogTypeEnum;
import com.lxl.common.vo.ResponseInfo;
import com.lxl.web.mq.RocketMqConsumer;
import com.lxl.web.utils.HttpServletUtils;
import com.lxl.web.utils.IpUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 日志操作默认实现类
 */
@Component
public class DefaultLogPoint implements LogPoint {

    @Autowired
    private RocketMqConsumer rocketMqConsumer;

    @Override
    public void saveLog(ProceedingJoinPoint joinPoint, LogTypeEnum logTypeEnum, ResponseInfo responseInfo) {
        Object arg = joinPoint.getArgs();
        Log log = operateLog(joinPoint, logTypeEnum, responseInfo, HttpServletUtils.getRequest());
        if (log != null) {
            //            rocketMqConsumer.sendMsg(JSON.toJSONString(log), MqTagsEnum.LOG);
            LOGGER.info("日志已发往mq处理。。。");
        }
    }

    /**
     * 生成操作日志
     *
     * @param joinPoint
     * @param logTypeEnum
     * @param request
     * @return
     */
    @Override
    public Log operateLog(ProceedingJoinPoint joinPoint, LogTypeEnum logTypeEnum, ResponseInfo responseInfo, HttpServletRequest request) {
        try {
            String pattern = logTypeEnum.getPattern();
            String creatorName = null;
            String creatorId = null;
            Log log = new Log();
            String status = responseInfo != null && responseInfo.getSuccess() ? "操作成功" : "操作失败";
            if (logTypeEnum.equals(LogTypeEnum.LOGIN_LOG)) {
                // 登录日志
                status = responseInfo != null && responseInfo.getSuccess() ? "登录成功" : "登录失败";
            } else {
                // 默认系统日志 根据业务操作
            }
            log.setStatus(responseInfo != null && responseInfo.getSuccess());
            log.setContent(String.format(pattern, creatorName, IpUtils.getIpAddr(request), status));
            log.setLogType(logTypeEnum.getLogName());
            log.setCreatorName(creatorName);
            log.setCreatorId(creatorId);
            log.setCreateTime(new Date());
            LOGGER.info("生成操作日志：{}", JSON.toJSONString(log));
            return log;
        } catch (Throwable throwable) {
            LOGGER.error("日志处理异常：", throwable);
            return null;
        }
    }
}
