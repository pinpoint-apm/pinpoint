package com.profiler.dto;

import org.junit.Test;

import com.profiler.common.dto.Header;

import java.util.logging.Logger;

public class HeaderTest {

    private final Logger logger = Logger.getLogger(Header.class.getName());


    @Test
    public void testGetSignature() throws Exception {
        Header header = new Header();
        byte signature = header.getSignature();
        System.out.println(signature);
        short type = header.getType();
        byte version = header.getVersion();
        System.out.println(version);
    }
}
