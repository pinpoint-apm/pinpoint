package com.navercorp.pinpoint.grpc.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

public class SocketAddressUtilsTest {

    @Test
    public void testTestToString() {
        InetSocketAddress unresolved = InetSocketAddress.createUnresolved("127.0.0.1", 9999);
        String address = SocketAddressUtils.toString(unresolved);
        Assertions.assertEquals("127.0.0.1:9999", address);
    }
}