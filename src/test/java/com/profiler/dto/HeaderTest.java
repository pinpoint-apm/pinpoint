package com.profiler.dto;

import com.profiler.common.dto.Header;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HeaderTest {

    private final Logger logger = LoggerFactory.getLogger(Header.class.getName());


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
