/*
 * Copyright 2016 zhichubao.com All right reserved. This software is the
 * confidential and proprietary information of zhichubao.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with zhichubao.com .
 */
package com.github.acticfox.common.tools.thredpool;

/**
 * 类ThreadPoolConfig.java的实现描述：获取线程池的大小配置
 *
 * @author fanyong.kfy 2016年12月1日 下午2:16:25
 */
public interface ThreadPoolConfig {

    int getThreadNum(String threadPoolName);

}
