package com.nhn.pinpoint.profiler.util.bindvalue;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

public class BindValueConverterTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testBindValueToString() throws Exception {
        Date d = new Date();
        logger.debug("{}", d);

        byte[] bytes = new byte[] {1, 2, 4};
        String s = Arrays.toString(bytes);
        logger.debug(s);
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
