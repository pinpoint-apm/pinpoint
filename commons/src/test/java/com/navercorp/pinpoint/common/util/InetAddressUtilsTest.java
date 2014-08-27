package com.nhn.pinpoint.common.util;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 */
public class InetAddressUtilsTest {
    @Test
    public void test() throws UnknownHostException {
        InetAddress byName = InetAddress.getByName("0:0:0:0:0:0:0:1");

        System.out.println(byName);
        System.out.println(byName.getAddress().length);

        InetAddress ipv4= InetAddress.getByName("127.0.0.1");
        System.out.println(ipv4);
    }
}
