package com.navercorp.pinpoint.profiler.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HexUtilsTest {

    @Test
    public void toHexString() {
        byte[] bytes = new byte[] {1, 2, 3};
        String s = HexUtils.toHexString(bytes, 16);
        Assertions.assertEquals("010203", s);
    }

    @Test
    public void toHexString2() {
        byte[] bytes = new byte[] {1, 2, 3};
        String s = HexUtils.toHexString(bytes, 1);
        Assertions.assertEquals("01...", s);
    }
}