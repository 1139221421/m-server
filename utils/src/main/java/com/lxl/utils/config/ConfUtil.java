package com.lxl.utils.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置类工具
 */
public class ConfUtil {
    private static Logger logger = LoggerFactory.getLogger(ConfUtil.class);
    private static final String ZKSTR = System.getProperty("project.zkstr");
    private static final String PROJECT_NAME = System.getProperty("project.name");
    private static final String PROJECT_PROFILE = System.getProperty("project.profile");
    private static final String PROJECT_VERSION = System.getProperty("project.version");
    private static CuratorFramework client = null;
    private static final String PATH = "/disconf/" + PROJECT_NAME + "_" + PROJECT_VERSION + "_" + PROJECT_PROFILE + "/item";

    public static CuratorFramework getCuratorClient() {
        client.create().withMode(CreateMode.EPHEMERAL).inBackground((curatorFramework, curatorEvent) -> {
        });
        return client;
    }

    public static void initAndSetProperties() throws Exception {
        if (StringUtils.isEmpty(ZKSTR) || StringUtils.isEmpty(PROJECT_NAME)
                || StringUtils.isEmpty(PROJECT_PROFILE) || StringUtils.isEmpty(PROJECT_VERSION)
        ) {
            logger.error("Please Set Env Variable!");
            System.exit(-1);
        }
        long a = System.currentTimeMillis();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(ZKSTR)
                .connectionTimeoutMs(3000)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();
        logger.info("build zk:{}", (System.currentTimeMillis() - a));
        long b = System.currentTimeMillis();
        client.start();
        logger.info("connect zk:{}", (System.currentTimeMillis() - b));
        PathChildrenCache cache = new PathChildrenCache(client, PATH, true);
        cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        cache.getCurrentData().forEach(childData -> {
            String key = childData.getPath().substring(childData.getPath().lastIndexOf("/") + 1);
            System.setProperty(key, new String(childData.getData()));
        });

        cache.getListenable().addListener((client1, event) -> {
            //监听节点的变化
            if (event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED
                    || event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                String key = event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/") + 1);
                logger.info("Property Of Key:{} Is Updated！The New Value Is {}", key, new String(event.getData().getData()));
                System.setProperty(key, new String(event.getData().getData()));
            }
        });
    }

    /**
     * 获取默认值
     *
     * @param key
     * @param def 默认值
     * @return
     */
    public static String getPropertyOrDefault(String key, String def) {
        try {
            return getProperty(key);
        } catch (Exception e) {
            return def;
        }
    }

    public static String getProperty(String key) throws Exception {
        String value = System.getProperty(key);
        if (StringUtils.isEmpty(value)) {
            //若为空，从zk上取数据
            if (client != null) {
                Stat stat = client.checkExists().forPath(PATH + "/" + key);
                if (stat == null) {
                    throw new Exception("not found the Property of the Key:" + key);
                }
                byte[] bytes = client.getData().forPath(PATH + "/" + key);
                value = new String(bytes);
                System.setProperty(key, value);
                return value;
            } else {
                initAndSetProperties();
                value = System.getProperty(key);
                if (StringUtils.isEmpty(System.getProperty(key))) {
                    throw new Exception("not found the Property of the Key:" + key);
                }
            }
        }
        return value;
    }
}
