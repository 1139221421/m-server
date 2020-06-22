package com.lxl.web.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lxl.web.annotations.DisLockDeal;
import com.lxl.web.lock.DistLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 分布式锁获取切面操作
 */
@Aspect
@Component
public class DisLockAspect extends AspectBase {

    @Resource
    private DistLock distLock;

    @Around(value = "@annotation(com.lxl.web.annotations.DisLockDeal)")
    public Object getDisLockWithBusiness(ProceedingJoinPoint pjp) throws Throwable {
        Method currentMethod = currentMethod(pjp);
        DisLockDeal disLockDeal = currentMethod.getAnnotation(DisLockDeal.class);
        String lockIdName = disLockDeal.lockIdName();
        String lockId = "";
        JSONObject jsonObject = pjp.getArgs() == null || pjp.getArgs().length == 0 ? null : JSON.parseObject(JSON.toJSONString(pjp.getArgs()[0]));
        if (jsonObject == null) {
            lockId = lockIdName;
        } else if (lockIdName.contains(".")) {
            //有更深的一层
            String[] split = lockIdName.split("\\.");
            if (jsonObject.get(split[0]) instanceof JSONArray) {
                //若是数组，则循环所有值
                JSONArray objects = (JSONArray) jsonObject.get(split[0]);
                if (StringUtils.isEmpty(((JSONObject) objects.get(0)).getString(split[1]))) {
                    throw new RuntimeException("分布式锁获取不成功，未能成功获取到lockId");
                }
                for (int i = 0; i < objects.size(); i++) {
                    lockId = lockId + ((JSONObject) objects.get(i)).getString(split[1]);
                }
            }
        } else {
            if (StringUtils.isEmpty(jsonObject.getString(lockIdName))) {
                throw new RuntimeException("分布式锁获取不成功，未能成功获取到lockId");
            }
            lockId = jsonObject.getString(lockIdName);
        }

        try {
            if (distLock.lock(disLockDeal.action().getTagName(), lockId)) {
                return pjp.proceed();
            }
        } finally {
            distLock.unlock(disLockDeal.action().getTagName(), lockId);
        }
        return null;
    }


}
