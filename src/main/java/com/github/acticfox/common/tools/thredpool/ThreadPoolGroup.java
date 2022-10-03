package com.github.acticfox.common.tools.thredpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang.StringUtils;

/**
 * 类的实现描述：线程池组
 *
 * @author fanyong.kfy 2018-09-20 11:52:13
 */
public class ThreadPoolGroup {

    private final static String DEFAULT_GROUP_NAME = "defaultGroupName";

    private int threadPoolNum = 1;

    private int threadNumPerThreadPool = 1;

    private String groupName;

    private List<ThreadPoolExecutor> threadPoolExecutorList = new ArrayList<ThreadPoolExecutor>();

    private final AtomicInteger currThreadPoolIndex = new AtomicInteger(-1);

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public int getThreadPoolNum() {
        return threadPoolNum;
    }

    public void setThreadPoolNum(int threadPoolNum) {
        checkNotInitialized();
        this.threadPoolNum = threadPoolNum;
    }

    public int getThreadNumPerThreadPool() {
        return threadNumPerThreadPool;
    }

    public void setThreadNumPerThreadPool(int threadNumPerThreadPool) {
        checkNotInitialized();
        this.threadNumPerThreadPool = threadNumPerThreadPool;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        checkNotInitialized();
        this.groupName = groupName;
    }

    public void init() {
        if (initialized.compareAndSet(false, true)) {
            String tempGroupName = StringUtils.isBlank(groupName) ? DEFAULT_GROUP_NAME : groupName;
            for (int n = 0; n < threadPoolNum; n++) {
                threadPoolExecutorList.add(
                    createThreadPoolExecutor(threadNumPerThreadPool, tempGroupName + "-threadPool-" + (n + 1)));
            }
        }
    }

    private void checkNotInitialized() {
        if (initialized.get()) {
            throw new IllegalStateException(
                "ThreadPoolGroup " + groupName + " is initialized, operation not supported");
        }
    }

    private void checkInitialized() {
        if (!initialized.get()) {
            throw new IllegalStateException(
                "ThreadPoolGroup " + groupName + " is not initialized, operation not supported");
        }
    }

    public void shutdown() {
        checkInitialized();
        for (ThreadPoolExecutor threadPoolExecutor : threadPoolExecutorList) {
            threadPoolExecutor.shutdown();
        }
    }

    private ThreadPoolExecutor createThreadPoolExecutor(final int threadNum, String threadPoolName) {
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum must be greater than zero");
        }
        if (StringUtils.isBlank(threadPoolName)) {
            throw new IllegalArgumentException("threadPoolName is blank");
        }

        ThreadPoolExecutor threadPoolExecutor = ThreadPoolFactory.createThreadPool(threadPoolName,
            new ThreadPoolConfig() {
                @Override
                public int getThreadNum(String threadPoolName) {
                    return threadNum;
                }
            });

        return threadPoolExecutor;
    }

    private ThreadPoolExecutor fetchThreadPoolExecutor() {
        return threadPoolExecutorList.get(fetchIndex());
    }

    private int fetchIndex() {
        while (true) {
            int current = currThreadPoolIndex.incrementAndGet();
            if (current >= threadPoolNum) {
                int head = 0;
                if (currThreadPoolIndex.compareAndSet(current, head)) {
                    return head;
                } else {
                    LockSupport.parkUntil(System.currentTimeMillis() + 5);
                }
            } else {
                return current;
            }
        }
    }

    /**
     * callableList 中每个Callable正常运行时及没有抛出异常时此方法会正常返回，如果碰到异常则会直接抛出
     *
     * @param callableList
     * @param <R>
     * @return
     */
    public <R> List<R> process(List<Callable<R>> callableList) {
        checkInitialized();
        ThreadPoolExecutor threadPoolExecutor = fetchThreadPoolExecutor();
        return ThreadPoolUtil.processInThreadPool(callableList, threadPoolExecutor);
    }

    /**
     * callableList中每个Callable的结果会通过CallResult返回，及时Callable抛出异常也会返回包含异常的CallableResult
     *
     * @param callableList
     * @param <R>
     * @return
     */
    public <R> List<CallResult<R>> execute(List<Callable<R>> callableList) {
        checkInitialized();
        ThreadPoolExecutor threadPoolExecutor = fetchThreadPoolExecutor();
        return ThreadPoolUtil.executeInThreadPool(callableList, threadPoolExecutor);
    }

}

