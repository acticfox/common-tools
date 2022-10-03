package com.github.acticfox.common.tools.spring;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * 类AbstractStarter.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Jul 29, 2020 9:59:04 AM
 */
public abstract class AbstractStarter implements InitializingBean, ConcreteContextListener {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected AtomicBoolean ifStarted = new AtomicBoolean(false);

    @Override
    public void afterPropertiesSet() throws Exception {
        ContextListenerManager.addListener(this);
    }

    @Override
    public void onContextRefreshed() throws Exception {
        if (ifStarted.compareAndSet(false, true)) {
            log.info("{} start begin", this.getClass().getName());
            start();
            log.info("{} start done", this.getClass().getName());
        }
    }

    /**
     * spring context fresh后调用
     *
     * @throws Exception
     */
    public abstract void start() throws Exception;

    @Override
    public void onContextClosed() throws Exception {
        if (ifStarted.compareAndSet(true, false)) {
            log.info("{} shutdown begin", this.getClass().getName());
            shutdown();
            log.info("{} shutdown done", this.getClass().getName());
        }
    }

    /**
     * spring context close后调用
     *
     * @throws Exception
     */
    public abstract void shutdown() throws Exception;
}
