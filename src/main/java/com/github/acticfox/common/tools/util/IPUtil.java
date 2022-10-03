/*
 * Copyright 2015 zhichubao.com All right reserved. This software is the
 * confidential and proprietary information of zhichubao.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with zhichubao.com .
 */
package com.github.acticfox.common.tools.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import org.apache.commons.lang.StringUtils;

/**
 * 类IPUtil.java的实现描述：
 * 
 * <pre>
 * IP获取远程客户端IP、本机IP操作工具
 * </pre>
 * 
 * @author fanyong.kfy 2015年2月13日 下午2:35:55
 */
public class IPUtil {

    /**
     * 获取客户端真实IP
     * 
     * @param request
     * @return
     */
    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("NS-Client-IP");
        if (StringUtils.isBlank(ip) || !InetAddresses.isInetAddress(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isBlank(ip) || !InetAddresses.isInetAddress(ip)) {
            ip = request.getHeader("x-forwarded-for");
            String[] ips = StringUtils.split(ip, ",");
            if (ips != null) {
                for (String sip : ips) {
                    if (InetAddresses.isInetAddress(ip)) {
                        ip = sip;
                        break;
                    }
                }
            }
        }
        if (StringUtils.isBlank(ip) || !InetAddresses.isInetAddress(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || !InetAddresses.isInetAddress(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || !InetAddresses.isInetAddress(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    public static Collection<InetAddress> getAllHostAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            Collection<InetAddress> addresses = new ArrayList<InetAddress>();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    addresses.add(inetAddress);
                }
            }

            return addresses;
        } catch (SocketException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Collection<String> getAllIpv4NoLoopbackAddresses() {
        Collection<String> noLoopbackAddresses = new ArrayList<String>();
        Collection<InetAddress> allInetAddresses = getAllHostAddress();
        for (InetAddress address : allInetAddresses) {
            if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                noLoopbackAddresses.add(address.getHostAddress());
            }
        }

        return noLoopbackAddresses;
    }

    public static String getFirstNoLoopbackAddress() {
        Collection<String> allNoLoopbackAddresses = getAllIpv4NoLoopbackAddresses();
        Preconditions.checkState(!allNoLoopbackAddresses.isEmpty(), " Sorry, seems you don't have a network card!");

        return allNoLoopbackAddresses.iterator().next();
    }

    public static String getLocalHostName() {
        try {
            InetAddress netAddress = InetAddress.getLocalHost();
            return netAddress.getHostName();
        } catch (UnknownHostException e) {
            return "";
        }
    }

    public static String getLocalIp() {
        try {
            InetAddress netAddress = InetAddress.getLocalHost();
            return netAddress.getHostAddress();
        } catch (UnknownHostException e) {
            return "";
        }
    }

}
