package com.lxl.web.lock;

import com.lxl.utils.config.ConfUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMultiLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 */
public class DistLock {
    private static Logger LOG = LoggerFactory.getLogger(DistLock.class);

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
        } catch (Exception e) {
            e.printStackTrace();
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
            LOG.info("获取分布式锁成功,uniqueLockId:{}", uniqueLockId);
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
                LOG.info("单独解锁成功：" + uniqueLockId);
            } catch (Exception e) {
                LOG.error("单独解锁异常：" + e.getMessage(), e);
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
                LOG.info("批量解锁成功：" + batchKey);
            } catch (Exception e) {
                LOG.error("批量解锁异常：" + e.getMessage(), e);
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
}
