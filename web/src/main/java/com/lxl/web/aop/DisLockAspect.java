package com.lxl.web.aop;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
        if (StringUtils.isEmpty(lock)) {
            log.error("分布式锁获取失败：lock为空");
            throw new RuntimeException(CodeEnum.PARAM_ERROR.getMessage());
        }
        String lockId = null;
        if (pjp.getArgs() == null || pjp.getArgs().length == 0 || !lock.contains("#p")) {
            lockId = lock;
        } else {
            String[] arr = lock.split("\\.");
            String p = arr[0].replace("#p", "");
            if (!NumberUtil.isNumber(p)) {
                log.error("分布式锁获取失败：lock-#p参数有误");
                throw new RuntimeException(CodeEnum.PARAM_ERROR.getMessage());
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(pjp.getArgs()[Integer.parseInt(p)]));
            int len = arr.length;
            if (len > 1) {
                // 获取下面的属性 暂不考虑数组情况
                for (int i = 1; i < len; i++) {
                    if (i < len - 1 && !(jsonObject.get(arr[i]) instanceof JSONArray)) {
                        jsonObject = jsonObject.getJSONObject(arr[i]);
                    } else {
                        lockId = JSON.toJSONString(jsonObject.get(arr[i]));
                    }
                }
            } else {
                lockId = jsonObject.toJSONString();
            }
        }
        if (StringUtils.isEmpty(lockId)) {
            log.error("分布式锁获取失败：未获取到lockId");
            throw new RuntimeException(CodeEnum.PARAM_ERROR.getMessage());
        }

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
