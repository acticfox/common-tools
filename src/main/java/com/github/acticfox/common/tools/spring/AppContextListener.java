package com.github.acticfox.common.tools.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 类的实现描述：TODO 类实现描述
 *
 * @author fanyong.kfy 2018-08-22 14:58:37
 */
public class AppContextListener implements ApplicationListener {

    private final static Logger log = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            log.info("onContextRefreshed begin");
            onContextRefreshed();
            log.info("onContextRefreshed done");
        } else if (event instanceof ContextClosedEvent) {
            log.info("onContextClosed begin");
            onContextClosed();
            log.info("onContextClosed done");
        }
    }

    private void onContextRefreshed() {
        ContextListenerManager.onContextRefreshed();
    }

    private void onContextClosed() {
        ContextListenerManager.onContextClosed();
    }
}
