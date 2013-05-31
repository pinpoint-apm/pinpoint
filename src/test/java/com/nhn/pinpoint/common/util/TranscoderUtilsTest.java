package com.nhn.pinpoint.common.util;

import org.junit.Test;

import java.util.Arrays;

/**
 *
 */
public class TranscoderUtilsTest {
    @Test
    public void testEncodeNum() throws Exception {
        test(Long.MAX_VALUE - 1);
        test(1);

    }

    private void test(long test) {
        TranscoderUtils tu = new TranscoderUtils(true);
        byte[] bytes = tu.encodeLong(test);
        System.out.println("long:" + test + " type:" + Arrays.toString(bytes));
        long l = tu.decodeLong(bytes);
        System.out.println("long:" + l);
    }
}
