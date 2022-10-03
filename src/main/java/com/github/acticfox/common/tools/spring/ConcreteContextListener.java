package com.github.acticfox.common.tools.spring;

/**
 * 类的实现描述：ConcreteContextListener 类实现描述
 *
 * @author fanyong.kfy 2018-08-22 16:12:21
 */
public interface ConcreteContextListener {

    /**
     * 刷新的时候调用
     *
     * @throws Exception
     */
    void onContextRefreshed() throws Exception;

    /**
     * 关闭的时候调用
     *
     * @throws Exception
     */
    void onContextClosed() throws Exception;
}
