package com.github.acticfox.common.tools.thredpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类的实现描述：线程池执行相关的util方法
 *
 * @author fanyong.kfy 2018-09-25 10:49:51
 */
public class ThreadPoolUtil {

    private static Logger log = LoggerFactory.getLogger(ThreadPoolUtil.class);

    /**
     * callableList 中每个Callable正常运行时及没有抛出异常时此方法会正常返回，如果碰到异常则会直接抛出
     *
     * 当前调用线程会等待callableList中的每个Callable执行结束后再继续执行
     *
     * @param callableList
     * @param executorService
     * @param <R>
     * @return
     */
    public static <R> List<R> processInThreadPool(List<Callable<R>> callableList, ExecutorService executorService) {
        List<CallResult<R>> runResultList = executeInThreadPool(callableList, executorService);
        List<R> executeResultList = new ArrayList<R>();
        for (CallResult<R> callResult : runResultList) {
            if (callResult.getThrowable() != null) {
                if (callResult.getThrowable() instanceof RuntimeException) {
                    throw (RuntimeException)callResult.getThrowable();
                } else {
                    throw new RuntimeException(callResult.getThrowable().getMessage(), callResult.getThrowable());
                }
            }
            executeResultList.add(callResult.getResultData());
        }
        return executeResultList;
    }

    /**
     * callableList中每个Callable的结果会通过CallResult返回，及时Callable抛出异常也会返回包含异常的CallableResult
     *
     * @param callableList
     * @param executorService
     * @param <R>
     * @return
     */
    public static <R> List<CallResult<R>> executeInThreadPool(List<Callable<R>> callableList,
                                                              ExecutorService executorService) {
        List<Future<R>> futureList = new ArrayList<Future<R>>();

        for (Callable<R> callable : callableList) {
            Future<R> future = executorService.submit(callable);
            futureList.add(future);
        }

        List<CallResult<R>> resultList = new ArrayList<CallResult<R>>();
        for (Future<R> future : futureList) {
            try {
                R futureResult = future.get();
                resultList.add(CallResult.newSuccessResult(futureResult));
            } catch (InterruptedException e) {
                resultList.add(CallResult.newFailResult(e));
            } catch (ExecutionException e) {
                resultList.add(CallResult.newFailResult(e.getCause()));
            } catch (Exception e) {
                resultList.add(CallResult.newFailResult(e));
            }
        }
        return resultList;
    }

    /**
     * 获取第一个返回的非null的结果，即获取处理最快的结果，忽略调用异常
     *
     * @param callableList
     * @param executor
     * @param <R>
     * @return
     */
    public static <R> R fetchFirstNonnullResult(List<Callable<R>> callableList,
                                                Executor executor) {
        CompletionService<R> completionService = new ExecutorCompletionService<>(executor);
        List<Future<R>> futureList = new ArrayList<>();
        R result = null;
        try {
            for (Callable<R> s : callableList) {
                futureList.add(completionService.submit(s));
            }
            for (int i = 0; i < futureList.size(); i++) {
                try {
                    result = completionService.take().get();
                    if (result != null) {
                        break;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("fetchFirstNonnullResult error", e);
                }
            }
        } finally {
            for (Future<R> future : futureList) {
                future.cancel(true);
            }
        }
        return result;
    }
}