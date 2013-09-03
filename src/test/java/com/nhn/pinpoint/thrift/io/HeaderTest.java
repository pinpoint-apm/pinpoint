package com.nhn.pinpoint.thrift.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HeaderTest {

    private final Logger logger = LoggerFactory.getLogger(Header.class.getName());


    public void testGetSignature() throws Exception {
        Header header = new Header();
        byte signature = header.getSignature();
        short type = header.getType();
        byte version = header.getVersion();
    }
}
