package com.github.acticfox.common.tools.thredpool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.acticfox.common.tools.thredpool.MultiTaskQueueThreadPool.TaskQueueConfig;

/**
 * 类的实现描述：TODO 类实现描述
 *
 * @author fanyong.kfy 2019-08-12 14:13:52
 */
public class MultiTaskQueueThreadPoolFactory {

    private static Logger logger = LoggerFactory.getLogger(MultiTaskQueueThreadPoolFactory.class);

    private static Map<String, MultiTaskQueueThreadPool> cachedThreadPools = new HashMap<>();

    /**
     * 创建MultiTaskQueueThreadPool并缓存下来，下次创建同名的MultiTaskQueueThreadPool时直接返回
     *
     * @param threadPoolName
     * @param threadPoolConfig
     * @param queueConfigs
     * @return
     */
    public static MultiTaskQueueThreadPool createMultiTaskQueueThreadPool(String threadPoolName,
        ThreadPoolConfig threadPoolConfig,
        List<TaskQueueConfig> queueConfigs) {
        if (StringUtils.isBlank(threadPoolName)) {
            throw new IllegalArgumentException("threadPoolName is blank");
        }
        if (Objects.isNull(threadPoolConfig)) {
            throw new IllegalArgumentException("threadPoolConfig is null");
        }

        ThreadPoolExecutor threadPoolExecutor = ThreadPoolFactory.createThreadPool(threadPoolName, threadPoolConfig);

        MultiTaskQueueThreadPool multiTaskQueueThreadPool = cachedThreadPools.get(threadPoolName);
        if (multiTaskQueueThreadPool != null) {
            return multiTaskQueueThreadPool;
        } else {
            synchronized (cachedThreadPools) {
                multiTaskQueueThreadPool = cachedThreadPools.get(threadPoolName);
                if (multiTaskQueueThreadPool != null) {
                    return multiTaskQueueThreadPool;
                }
                multiTaskQueueThreadPool = new MultiTaskQueueThreadPool(threadPoolName, threadPoolExecutor,
                    queueConfigs);
                cachedThreadPools.put(threadPoolName, multiTaskQueueThreadPool);
                logger.info("multiTaskQueueThreadPool {} created", threadPoolName);
                return multiTaskQueueThreadPool;
            }
        }
    }
}
