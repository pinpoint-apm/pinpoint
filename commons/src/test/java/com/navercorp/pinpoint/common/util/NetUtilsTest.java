package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NetUtilsTest {

    @Test
    public void toInetSocketAddress() {
        InetSocketAddress inetSocketAddress = NetUtils.toInetSocketAddress("192.168.0.1:8081");
        Assertions.assertEquals(inetSocketAddress.getHostName(), "192.168.0.1");
        Assertions.assertEquals(inetSocketAddress.getPort(), 8081);
    }

    @Test
    public void toInetSocketAddress_miss_port1() {
        InetSocketAddress inetSocketAddress = NetUtils.toInetSocketAddress("192.168.0.1");
        Assertions.assertNull(inetSocketAddress);
    }

    @Test
    public void toInetSocketAddress_miss_port2() {
        InetSocketAddress inetSocketAddress = NetUtils.toInetSocketAddress("192.168.0.1:");
        Assertions.assertNull(inetSocketAddress);
    }

}