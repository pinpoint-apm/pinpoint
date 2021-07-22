package com.navercorp.pinpoint.grpc.server;

import junit.framework.TestCase;
import org.junit.Assert;

import java.net.InetSocketAddress;

public class SocketAddressUtilsTest extends TestCase {

    public void testTestToString() {
        InetSocketAddress unresolved = InetSocketAddress.createUnresolved("127.0.0.1", 9999);
        String address = SocketAddressUtils.toString(unresolved);
        Assert.assertEquals("127.0.0.1:9999", address);
    }
}