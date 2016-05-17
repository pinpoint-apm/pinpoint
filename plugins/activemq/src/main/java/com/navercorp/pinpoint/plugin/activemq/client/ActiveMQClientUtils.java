package com.navercorp.pinpoint.plugin.activemq.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author HyunGil Jeong
 */
public class ActiveMQClientUtils {

    private ActiveMQClientUtils() {
    }

    public static String getEndPoint(SocketAddress socketAddress) {
        String endPoint = ActiveMQClientConstants.UNKNOWN_ADDRESS;
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            InetAddress remoteAddress = inetSocketAddress.getAddress();
            if (remoteAddress != null) {
                endPoint = remoteAddress.getHostAddress() + ":" + inetSocketAddress.getPort();
            }
        }
        return endPoint;
    }
}