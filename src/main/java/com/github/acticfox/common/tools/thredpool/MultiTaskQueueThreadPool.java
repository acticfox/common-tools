package com.github.acticfox.common.tools.thredpool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * 类的实现描述：多个任务队列共享一个ThreadPool，每个任务队列可以单独设置capacity，公平的请求ThreadPool处理
 *
 * @author fanyong.kfy 2019-08-12 13:34:52
 */
public class MultiTaskQueueThreadPool {

    private static Logger logger = LoggerFactory.getLogger(MultiTaskQueueThreadPool.class);

    private String threadPoolName;

    private ThreadPoolExecutor threadPoolExecutor;

    private Map<String, LinkedBlockingQueue<FutureTask>> taskQueueMap = new HashMap<>();

    public MultiTaskQueueThreadPool(String threadPoolName, ThreadPoolExecutor threadPoolExecutor,
                                    List<TaskQueueConfig> queueConfigs) {
        if (StringUtils.isBlank(threadPoolName)) {
            throw new IllegalArgumentException("threadPoolName is blank");
        }
        if (Objects.isNull(threadPoolExecutor)) {
            throw new IllegalArgumentException("executorService is null");
        }
        if (CollectionUtils.isEmpty(queueConfigs)) {
            throw new IllegalArgumentException("queueNames is empty");
        }
        this.threadPoolName = threadPoolName;
        this.threadPoolExecutor = threadPoolExecutor;

        queueConfigs.forEach((queueConfig) -> {
            if (taskQueueMap.get(queueConfig.getQueueName()) == null) {
                LinkedBlockingQueue<FutureTask> queue = new LinkedBlockingQueue<>(queueConfig.queueCapacity);
                Thread queueConsumer = new Thread(() -> {
                    while (true) {
                        try {
                            FutureTask task = queue.take();
                            threadPoolExecutor.execute(task);
                        } catch (Throwable ex) {
                            logger.error("thread:{},errorMsg:{}", Thread.currentThread(), ex.getMessage(), ex);
                        }
                    }
                });
                queueConsumer.setName(this.threadPoolName + "_" + queueConfig.queueName + "_consumer");
                queueConsumer.start();
                taskQueueMap.put(queueConfig.getQueueName(), queue);
            }
        });
    }

    public static class TaskQueueConfig {

        private String queueName;
        private int queueCapacity;

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
    }

    /**
     * 获取queueName指定的任务队列，进而用于添加任务
     *
     * @param queueName
     * @return
     */
    private LinkedBlockingQueue<FutureTask> queueOf(String queueName) {
        return taskQueueMap.get(queueName);
    }

    public void putTask(String queueName, FutureTask task) throws InterruptedException {
        queueOf(queueName).put(task);
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if
     * necessary up to the specified wait time for space to become available.
     *
     * @param queueName
     * @param task
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    public boolean offerTask(String queueName, FutureTask task, long timeout, TimeUnit unit)
        throws InterruptedException {
        return queueOf(queueName).offer(task, timeout, unit);
    }

    public int queueSize(String queueName) {
        return queueOf(queueName).size();
    }

    public int getPoolSize() {
        return threadPoolExecutor.getPoolSize();
    }

    public int getCorePoolSize() {
        return threadPoolExecutor.getCorePoolSize();
    }

    public int getPoolActiveCount() {
        return threadPoolExecutor.getActiveCount();
    }

    public int getMaximumPoolSize() {
        return threadPoolExecutor.getMaximumPoolSize();
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }
}