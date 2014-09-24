package com.nhn.pinpoint.common.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 */
public class InetAddressUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void test() throws UnknownHostException {
        InetAddress byName = InetAddress.getByName("0:0:0:0:0:0:0:1");

        logger.debug("{}", byName);
        logger.debug("{}", byName.getAddress().length);

        InetAddress ipv4= InetAddress.getByName("127.0.0.1");
        logger.debug("{}", ipv4);
    }
}
