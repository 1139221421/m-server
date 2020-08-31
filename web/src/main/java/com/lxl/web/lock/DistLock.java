package com.lxl.web.lock;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.lxl.common.enums.CodeEnum;
import com.lxl.utils.config.ConfUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMultiLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 *
 * @author
 */
@Slf4j
public class DistLock {
    private static final String ROOT_PATH = "lock";

    private static final int DEFAULT_CONNECT_TIMEOUT = 2000;

    private static final int DEFAULT_SESSION_TIMEOUT = 2000;

    private static final int DEFAULT_LOCK_TIMEOUT = 10;

    private static final String BATCH = "_batch";

    private CuratorFramework client;

    private Map<String, InterProcessMutex> locks;

    private Map<String, InterProcessMultiLock> batchlocks;

    public DistLock() {
        try {
            this.client =
                    CuratorFrameworkFactory.builder().connectString(ConfUtil.getProperty("project.zkstr"))
                            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                            .connectionTimeoutMs(DEFAULT_CONNECT_TIMEOUT)
                            .sessionTimeoutMs(DEFAULT_SESSION_TIMEOUT).namespace(ROOT_PATH).build();
            this.client.start();
            locks = new HashMap<>(32);
            batchlocks = new HashMap<>(32);
            log.info("DistLock started...");
        } catch (Exception e) {
            log.error("DistLock error：", e);
            System.exit(-1);
        }
    }

    /**
     * 获取分布式锁
     *
     * @param action
     * @param lockId
     * @param time
     * @return
     * @throws Exception
     */
    public boolean lock(String action, String lockId, int time) throws Exception {
        String uniqueLockId = action + "_" + lockId + "_" + ConfUtil.getPropertyOrDefault("spring.profiles.active", "dev");
        InterProcessMutex lock = new InterProcessMutex(this.client, "/" + uniqueLockId);
        boolean isLocked = lock.acquire(time, TimeUnit.SECONDS);
        if (isLocked) {
            this.locks.put(uniqueLockId, lock);
            log.info("获取分布式锁成功,uniqueLockId:{}", uniqueLockId);
        } else {
            log.info("获取分布式锁失败,uniqueLockId:{}", uniqueLockId);
        }
        return isLocked;
    }

    /**
     * 获取分布式锁
     *
     * @param action
     * @param lockId
     * @return
     * @throws Exception
     */
    public boolean lock(String action, String lockId) throws Exception {
        return lock(action, lockId, DEFAULT_LOCK_TIMEOUT);
    }

    /**
     * 解锁
     *
     * @param action
     * @param lockId
     * @throws Exception
     */
    public void unlock(String action, String lockId) throws Exception {
        String uniqueLockId = action + "_" + lockId + "_" + ConfUtil.getPropertyOrDefault("spring.profiles.active", "dev");
        InterProcessMutex lock = null;
        if ((lock = this.locks.get(uniqueLockId)) != null) {
            try {
                this.locks.remove(uniqueLockId);
                lock.release();
                log.info("单独解锁成功：" + uniqueLockId);
            } catch (Exception e) {
                log.error("单独解锁异常：" + e.getMessage(), e);
                throw e;
            }
        }
    }

    /**
     * 批量加锁
     *
     * @param action
     * @param lockIds
     * @param time
     * @return
     * @throws Exception
     */
    public boolean batchLock(String action, Collection<String> lockIds, int time) throws Exception {
        List<String> realLockIds = new ArrayList<>();
        for (String lockId : lockIds) {
            String prefix = action.split("_")[0];
            realLockIds.add("/" + prefix + "_" + lockId + "_" + ConfUtil.getPropertyOrDefault("spring.profiles.active", "dev"));
        }
        InterProcessMultiLock mulLock = new InterProcessMultiLock(this.client, realLockIds);
        boolean isLocked = mulLock.acquire(time, TimeUnit.SECONDS);
        if (isLocked) {
            this.batchlocks.put(action + BATCH, mulLock);
        }
        return isLocked;
    }

    public boolean batchLock(String action, Collection<String> lockIds) throws Exception {
        return batchLock(action, lockIds, DEFAULT_LOCK_TIMEOUT);
    }

    /**
     * 批量解锁
     *
     * @param action
     * @throws Exception
     */
    public void unbatchLock(String action) throws Exception {
        String batchKey = action + BATCH;
        InterProcessMultiLock mulLock = null;
        if ((mulLock = this.batchlocks.get(batchKey)) != null) {
            try {
                this.batchlocks.remove(batchKey);
                mulLock.release();
                log.info("批量解锁成功：" + batchKey);
            } catch (Exception e) {
                log.error("批量解锁异常：" + e.getMessage(), e);
                throw e;
            }
        }
    }


    /**
     * 关闭客户端
     */
    public void close() {
        if (this.client != null) {
            CloseableUtils.closeQuietly(this.client);
        }
    }

    /**
     * 获取分布式锁lockId
     *
     * @param lock 锁参数：#p0.obj.id:第一个参数的obj字段下的id字段值作为lockId
     * @param pjp  切入点
     * @return
     */
    public String getLockId(String lock, ProceedingJoinPoint pjp) {
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
            String arg = JSON.toJSONString(pjp.getArgs()[Integer.parseInt(p)]);
            try {
                JSONObject jsonObject = JSON.parseObject(arg);
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
            } catch (JSONException e) {
                // 不是json对象
                lockId = arg;
            }
        }
        if (StringUtils.isBlank(lockId)) {
            log.error("分布式锁获取失败：未获取到lockId");
            throw new RuntimeException(CodeEnum.PARAM_ERROR.getMessage());
        }
        return lockId;
    }
}
