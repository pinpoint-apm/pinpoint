package com.profiler.dto;

import org.junit.Test;

public class HeaderTest {
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
