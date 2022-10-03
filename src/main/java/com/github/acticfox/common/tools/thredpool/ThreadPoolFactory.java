/*
 * Copyright 2016 github.com All right reserved. This software is the
 * confidential and proprietary information of github.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with github.com .
 */
package com.github.acticfox.common.tools.thredpool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类ThreadPoolFactory.java的实现描述：线程池工厂类，根据ThreadPoolConfig可以动态设置大小
 *
 * @author fanyong.kfy 2016年11月30日 下午3:58:43
 */
public class ThreadPoolFactory {

    private final static Logger logger = LoggerFactory.getLogger(ThreadPoolFactory.class);

    private static Map<String, ThreadPoolExecutor> threadPoolMap = new HashMap<String, ThreadPoolExecutor>();

    private static void updateThreadNumDynamically(String threadPoolName, ThreadPoolConfig threadPoolConfig,
                                                   ThreadPoolExecutor threadPoolExecutor) {

        int configedThreadNum = threadPoolConfig.getThreadNum(threadPoolName);
        int currThreadPoolThreadNum = threadPoolExecutor.getMaximumPoolSize();

        if (configedThreadNum != currThreadPoolThreadNum) {

            int corePoolSize = threadPoolExecutor.getCorePoolSize();
            int maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
            int poolSize = threadPoolExecutor.getPoolSize();
            int activeCount = threadPoolExecutor.getActiveCount();

            threadPoolExecutor.setCorePoolSize(configedThreadNum);
            threadPoolExecutor.setMaximumPoolSize(configedThreadNum);

            int newCorePoolSize = threadPoolExecutor.getCorePoolSize();
            int newMaxPoolSize = threadPoolExecutor.getMaximumPoolSize();
            int newPoolSize = threadPoolExecutor.getPoolSize();
            int newActiveCount = threadPoolExecutor.getActiveCount();

            logger.info(
                "threadNumUpdated threadPoolName:{},corePoolSize:{} to {} ,maxPoolSize:{} to {},poolSize:{} to {},"
                    + "activeCount:{} to {},currentThread:{}",
                threadPoolName, corePoolSize, newCorePoolSize, maxPoolSize, newMaxPoolSize, poolSize, newPoolSize,
                activeCount, newActiveCount, Thread.currentThread());
        }
    }

    public static ThreadPoolExecutor createThreadPool(String threadPoolName, ThreadPoolConfig threadPoolConfig) {
        return createThreadPool(threadPoolName, threadPoolConfig, null);
    }

    public static ThreadPoolExecutor createThreadPool(String threadPoolName, ThreadPoolConfig threadPoolConfig,
                                                      RejectedExecutionHandler handler) {
        if (StringUtils.isBlank(threadPoolName)) {
            throw new IllegalArgumentException("threadPoolName is null");
        }
        if (threadPoolConfig == null) {
            throw new IllegalArgumentException("threadPoolConfig is null");
        }

        ThreadPoolExecutor threadPoolExecutor = threadPoolMap.get(threadPoolName);
        if (threadPoolExecutor != null) {
            updateThreadNumDynamically(threadPoolName, threadPoolConfig, threadPoolExecutor);
            logger.info("return cached threadPool:{},currentThread:{}", threadPoolName, Thread.currentThread());
            return threadPoolExecutor;
        } else {
            synchronized (threadPoolMap) {
                threadPoolExecutor = threadPoolMap.get(threadPoolName);
                if (threadPoolExecutor != null) {
                    updateThreadNumDynamically(threadPoolName, threadPoolConfig, threadPoolExecutor);
                    logger.info("return cached threadPool:{},currentThread:{},in synchronized block", threadPoolName,
                        Thread.currentThread());
                    return threadPoolExecutor;
                } else {
                    int initThreadNum = threadPoolConfig.getThreadNum(threadPoolName);

                    threadPoolExecutor = new ThreadPoolExecutor(initThreadNum, initThreadNum, 0L,
                        TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(true), new NamedThreadFactory(
                        threadPoolName), handler == null ? new WaitingEnqueuePolicy() : handler);
                    threadPoolMap.put(threadPoolName, threadPoolExecutor);

                    logger.info("return created threadPool:{},currentThread:{}", threadPoolName,
                        Thread.currentThread());
                    return threadPoolExecutor;
                }
            }
        }
    }
}
