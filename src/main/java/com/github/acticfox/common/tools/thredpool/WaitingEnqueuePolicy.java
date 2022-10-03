package com.github.acticfox.common.tools.thredpool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 类的实现描述：等待加入队列
 *
 * @author fanyong.kfy 2018-02-13 14:15:37
 */
public class WaitingEnqueuePolicy implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            if (!executor.isShutdown()) {
                executor.getQueue().put(r);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
