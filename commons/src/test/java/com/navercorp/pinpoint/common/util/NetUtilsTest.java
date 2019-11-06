package com.navercorp.pinpoint.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NetUtilsTest {

    @Test
    public void toInetSocketAddress() {
        InetSocketAddress inetSocketAddress = NetUtils.toInetSocketAddress("192.168.0.1:8081");
        Assert.assertEquals(inetSocketAddress.getHostName(), "192.168.0.1");
        Assert.assertEquals(inetSocketAddress.getPort(), 8081);
    }

    @Test
    public void toInetSocketAddress_miss_port1() {
        InetSocketAddress inetSocketAddress = NetUtils.toInetSocketAddress("192.168.0.1");
        Assert.assertNull(inetSocketAddress);
    }

    @Test
    public void toInetSocketAddress_miss_port2() {
        InetSocketAddress inetSocketAddress = NetUtils.toInetSocketAddress("192.168.0.1:");
        Assert.assertNull(inetSocketAddress);
    }

}