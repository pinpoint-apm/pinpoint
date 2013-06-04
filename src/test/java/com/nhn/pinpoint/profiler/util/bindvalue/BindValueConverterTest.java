package com.nhn.pinpoint.profiler.util.bindvalue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

public class BindValueConverterTest {
    @Test
    public void testBindValueToString() throws Exception {
        Date d = new Date();
        System.out.println(d);

        byte[] bytes = new byte[] {1, 2, 4};
        String s = Arrays.toString(bytes);
        System.out.println(s);
    }
}
