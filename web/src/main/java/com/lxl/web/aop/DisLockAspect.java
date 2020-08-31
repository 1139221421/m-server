package com.lxl.web.aop;

import com.lxl.common.enums.CodeEnum;
import com.lxl.web.annotations.DisLockDeal;
import com.lxl.web.lock.DistLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 分布式锁获取切面操作
 */
@Slf4j
@Aspect
@Component
public class DisLockAspect extends AspectBase {

    @Resource
    private DistLock distLock;

    @Around(value = "@annotation(com.lxl.web.annotations.DisLockDeal)")
    public Object getDisLockWithBusiness(ProceedingJoinPoint pjp) throws Throwable {
        Method currentMethod = currentMethod(pjp);
        DisLockDeal disLockDeal = currentMethod.getAnnotation(DisLockDeal.class);
        String lock = disLockDeal.lock();
        if (StringUtils.isBlank(lock)) {
            log.error("分布式锁获取失败：lock为空");
            throw new RuntimeException(CodeEnum.PARAM_ERROR.getMessage());
        }
        String lockId = distLock.getLockId(lock, pjp);
        try {
            if (distLock.lock(disLockDeal.tag().getTagName(), lockId)) {
                return pjp.proceed();
            }
        } finally {
            distLock.unlock(disLockDeal.tag().getTagName(), lockId);
        }
        return null;
    }

}
