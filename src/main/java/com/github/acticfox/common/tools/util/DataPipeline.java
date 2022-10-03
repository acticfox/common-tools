package com.github.acticfox.common.tools.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.acticfox.common.tools.thredpool.ThreadPoolUtil;

/**
 * 类的实现描述：分步的多线程的数据处理管道
 *
 * @author fanyong.kfy 2019-08-09 10:28:09
 */
public class DataPipeline<T> {

    private static final Logger logger = LoggerFactory.getLogger(DataPipeline.class);

    private List<HandlerConfig<T>> handlerConfigs = new ArrayList<>();

    private DataPipeline() {}

    public static <T> DataPipeline<T> newPipeline() {
        return new DataPipeline<>();
    }

    public void pushData(List<T> dataList) {
        handlerConfigs.forEach((d) -> {
            handleData(dataList, d);
        });
    }

    private void handleData(List<T> dataList, HandlerConfig handlerConfig) {
        if (handlerConfig.handlerTransferType == HandlerTransferType.SYNC) {
            List<Callable<Void>> callableList = dataList.stream().map((d) -> (Callable<Void>)() -> {
                handlerConfig.dataHandler.handle(d);
                return null;
            }).collect(Collectors.toList());
            ThreadPoolUtil.executeInThreadPool(callableList, handlerConfig.executorService);
        } else {
            dataList.forEach((d) -> {
                handlerConfig.executorService.execute(() -> {
                    handlerConfig.dataHandler.handle(d);
                });
            });
        }
    }

    public DataPipeline addHandler(DataHandler<T> dataHandler, ExecutorService executorService) {
        return this.addHandler(dataHandler, executorService, HandlerTransferType.SYNC);
    }

    public DataPipeline addHandler(DataHandler<T> dataHandler, ExecutorService executorService,
        HandlerTransferType handlerTransferType) {
        Objects.requireNonNull(dataHandler, "dataHandler is null");
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(handlerTransferType, "handlerTransferType is null");

        HandlerConfig config = new HandlerConfig();
        config.dataHandler = new DataHandlerWrapper(dataHandler);
        config.executorService = executorService;
        config.handlerTransferType = handlerTransferType;
        handlerConfigs.add(config);
        return this;
    }

    @FunctionalInterface
    public interface DataHandler<T> {
        void handle(T t);
    }

    public enum HandlerTransferType {
        /**
         * 当前handler针对每条数据处理都处理完才进入下一个handler进行处理
         */
        SYNC,
        /**
         * 当前handler针对每条数据处理只要能放入到线程池就进入下一个handler处理
         */
        ASYNC;
    }

    private class HandlerConfig<T> {
        DataHandler<T> dataHandler;
        ExecutorService executorService;
        HandlerTransferType handlerTransferType;
    }

    private class DataHandlerWrapper<T> implements DataHandler<T> {
        DataHandler<T> dataHandler;

        public DataHandlerWrapper(DataHandler<T> dataHandler) {
            this.dataHandler = dataHandler;
        }

        @Override
        public void handle(T t) {
            try {
                this.dataHandler.handle(t);
            } catch (Exception ex) {
                logger.error("dataHandler:{},exception caught,errorMsg:{}", dataHandler.getClass(), ex.getMessage(),
                    ex);
            }
        }
    }

}