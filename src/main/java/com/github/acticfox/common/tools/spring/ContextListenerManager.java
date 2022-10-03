package com.github.acticfox.common.tools.spring;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类的实现描述：ContextListenerManager 类实现描述
 *
 * @author fanyong.kfy 2018-08-22 16:14:26
 */
public class ContextListenerManager {

    private final static Logger log = LoggerFactory.getLogger(ContextListenerManager.class);

    private static List<ConcreteContextListener> listenerList = new ArrayList<>();

    public static void addListener(ConcreteContextListener listener) {
        listenerList.add(listener);
    }

    public static void onContextRefreshed() {
        for (ConcreteContextListener listener : listenerList) {
            try {
                log.info("{},onContextRefreshed begin", listener.getClass().getName());
                listener.onContextRefreshed();
                log.info("{},onContextRefreshed done", listener.getClass().getName());
            } catch (Exception ex) {
                log.error("{},onContextRefreshed error ,errorMsg:{}", listener.getClass().getName(), ex.getMessage(),
                    ex);
            }

        }
    }

    public static void onContextClosed() {
        for (ConcreteContextListener listener : listenerList) {
            try {
                log.info("{},onContextClosed begin", listener.getClass().getName());
                listener.onContextClosed();
                log.info("{},onContextClosed done", listener.getClass().getName());
            } catch (Exception ex) {
                log.error("{},onContextClosed error ,errorMsg:{}", listener.getClass().getName(), ex.getMessage(), ex);
            }
        }
    }

}
