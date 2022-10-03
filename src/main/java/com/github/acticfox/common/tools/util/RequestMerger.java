package com.github.acticfox.common.tools.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类的实现描述：请求合并
 *
 * @author fanyong.kfy 2019-08-13 09:22:38
 */
public class RequestMerger<T, R> {

    private static Logger logger = LoggerFactory.getLogger(RequestMerger.class);

    private LinkedBlockingQueue<Request<T, R>> queue;

    private int mergerCount;

    private long mergerMillisInterval;

    private int queueCapacity = 1;

    private MergerHandler mergerHandler;

    private static final int PARKING_TIME = 10;

    public RequestMerger(int mergerCount, long mergerMillisInterval,
                         int queueCapacity, MergerHandler<T, R> mergerHandler) {
        this.mergerCount = mergerCount;
        this.mergerMillisInterval = mergerMillisInterval;
        this.mergerHandler = mergerHandler;
        this.queueCapacity = queueCapacity;
        init();
    }

    void init() {
        queue = new LinkedBlockingQueue<>(queueCapacity);
        Thread consumer = new Thread(() -> {
            while (true) {
                try {
                    Request request = queue.peek();
                    int queueSize = queue.size();
                    boolean isTimeSatisfied = request != null
                        && (System.currentTimeMillis() - request.getEnqueueTime()) >= mergerMillisInterval;
                    boolean isCountSatisfied = queueSize >= mergerCount;
                    int minCount = Math.min(queueSize, mergerCount);

                    if (isTimeSatisfied || isCountSatisfied) {
                        List<Request> mergedRequests = new ArrayList<>();
                        for (int i = 0; i < minCount; i++) {
                            mergedRequests.add(queue.take());
                        }
                        mergerHandler.handle(mergedRequests);
                    } else {
                        LockSupport.parkUntil(System.currentTimeMillis() + PARKING_TIME);
                    }
                } catch (Throwable e) {
                    logger.error("requestMerger consumer error,errorMsg:{}", e.getMessage(), e);
                }
            }
        });
        consumer.setName("requestMergerConsumer");
        consumer.start();
    }

    public void putRequest(Request<T, R> request) throws InterruptedException {
        request.setEnqueueTime(System.currentTimeMillis());
        queue.put(request);
    }

    public boolean offerRequest(Request<T, R> request, int timeout, TimeUnit unit) throws InterruptedException {
        request.setEnqueueTime(System.currentTimeMillis());
        return queue.offer(request, timeout, unit);
    }

    public int queueSize() {
        return queue.size();
    }

    public static class Request<T, R> {

        private long enqueueTime;

        private T param;

        private R result;

        private FutureTask<R> futureTask;

        public Request() {
            init();
        }

        private void init() {
            futureTask = new FutureTask<>(() -> result);
        }

        public long getEnqueueTime() {
            return enqueueTime;
        }

        public void setEnqueueTime(long enqueueTime) {
            this.enqueueTime = enqueueTime;
        }

        public R getResult() throws ExecutionException, InterruptedException {
            return futureTask.get();
        }

        public R getResult(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            return futureTask.get(timeout, unit);
        }

        public void setResult(R result) {
            this.result = result;
            futureTask.run();
        }

        public T getParam() {
            return param;
        }

        public void setParam(T param) {
            this.param = param;
        }
    }

    public interface MergerHandler<T, R> {
        /**
         * 处理合并后的请求
         *
         * @param requests
         */
        void handle(List<Request<T, R>> requests);
    }
}