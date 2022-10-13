package com.cqx.netty.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * HostUtil
 *
 * @author chenqixu
 */
public class HostUtil {

    private static final Logger logger = LoggerFactory.getLogger(HostUtil.class);

    private static InetAddress getInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.info("unknown host!");
        }
        return null;
    }

    /***
     *  获取主机名
     */
    public static String getHostName() {
        InetAddress netAddress = getInetAddress();
        if (null == netAddress) {
            return null;
        }
        return netAddress.getHostName();
    }

    /***
     *  获取主机IP
     */
    public static String getHostIp() {
        InetAddress netAddress = getInetAddress();
        if (null == netAddress) {
            return null;
        }
        return netAddress.getHostAddress();
    }
}
