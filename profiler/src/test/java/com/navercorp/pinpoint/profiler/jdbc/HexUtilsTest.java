package com.navercorp.pinpoint.profiler.jdbc;

import org.junit.Assert;
import org.junit.Test;

public class HexUtilsTest {

    @Test
    public void toHexString() {
        byte[] bytes = new byte[] {1, 2, 3};
        String s = HexUtils.toHexString(bytes, 16);
        Assert.assertEquals("010203", s);
    }

    @Test
    public void toHexString2() {
        byte[] bytes = new byte[] {1, 2, 3};
        String s = HexUtils.toHexString(bytes, 1);
        Assert.assertEquals("01...", s);
    }
}