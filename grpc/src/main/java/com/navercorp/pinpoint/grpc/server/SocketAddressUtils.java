package com.navercorp.pinpoint.grpc.server;

import java.net.InetSocketAddress;

public final class SocketAddressUtils {
    private SocketAddressUtils() {
    }

    public static String toString(InetSocketAddress remoteAddress) {
        return remoteAddress.getHostString() + ":" + remoteAddress.getPort();
    }
}
