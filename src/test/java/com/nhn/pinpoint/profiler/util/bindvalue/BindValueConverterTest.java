package com.nhn.pinpoint.profiler.util.bindvalue;

import junit.framework.Assert;
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

    @Test
    public void testBindValueBoolean() throws Exception {
        String setBoolean = BindValueConverter.convert("setBoolean", new Object[]{null, Boolean.TRUE});
        Assert.assertEquals(setBoolean, Boolean.TRUE.toString());
    }

    @Test
    public void testBindValueNotSupport() throws Exception {
        // 지원되지 않는 api 라도 Exception이 발생하면 안됨.
        String setBoolean = BindValueConverter.convert("setXxxx", new Object[]{null, "XXX"});
        Assert.assertEquals(setBoolean, "");
    }
}
