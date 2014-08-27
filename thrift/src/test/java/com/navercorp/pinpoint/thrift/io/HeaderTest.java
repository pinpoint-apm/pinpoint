package com.nhn.pinpoint.thrift.io;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class HeaderTest {

    private final Logger logger = LoggerFactory.getLogger(Header.class.getName());

    @Test
    public void testGetSignature() throws Exception {
        Header header = new Header();
        byte signature = header.getSignature();
        short type = header.getType();
        byte version = header.getVersion();
    }
}
