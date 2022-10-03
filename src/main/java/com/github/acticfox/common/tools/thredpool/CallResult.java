package com.github.acticfox.common.tools.thredpool;

/**
 * 类的实现描述：Callable调用结果封装
 *
 * @author fanyong.kfy 2018-09-20 20:00:04
 */

public class CallResult<R> {

    private Throwable throwable;

    private R resultData;

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public R getResultData() {
        return resultData;
    }

    public void setResultData(R resultData) {
        this.resultData = resultData;
    }

    public static CallResult newFailResult(Throwable throwable) {
        CallResult callResult = new CallResult();
        callResult.setThrowable(throwable);
        return callResult;
    }

    public static <R> CallResult newSuccessResult(R resultData) {
        CallResult<R> callResult = new CallResult<R>();
        callResult.setResultData(resultData);
        return callResult;
    }

}