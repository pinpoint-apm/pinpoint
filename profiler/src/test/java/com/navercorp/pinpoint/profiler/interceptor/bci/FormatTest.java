package com.nhn.pinpoint.profiler.interceptor.bci;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Format;
import java.util.Formatter;

/**
 * @author emeroad
 */
public class FormatTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void format() {
        StringBuilder buffer = new StringBuilder();
        Formatter formatter = new Formatter(buffer);
        formatter.format("%1s", "ab");

        formatter.format("%3s", "a");
        Assert.assertEquals(buffer.toString(), "ab  a");
    }

    @Test
    public void format2() {
        StringBuilder buffer = new StringBuilder();
        Formatter formatter = new Formatter(buffer);
        formatter.format("(%s, %s, %s)", 1, 2, 3);

        Assert.assertEquals(buffer.toString(), "(1, 2, 3)");
    }
}
