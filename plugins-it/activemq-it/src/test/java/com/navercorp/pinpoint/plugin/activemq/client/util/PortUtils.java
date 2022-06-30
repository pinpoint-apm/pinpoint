package com.navercorp.pinpoint.plugin.activemq.client.util;

import com.navercorp.pinpoint.testcase.util.SocketUtils;

public final class PortUtils {
    public static final String DEFAULT_HOST = "tcp://127.0.0.1";
    public static final int DEFAULT_PORT = 61616;

    public static String findAvailableUrl(int minPort) {
        int port = SocketUtils.findAvailableTcpPort(minPort);
        return DEFAULT_HOST + ":" + port;
    }

}
