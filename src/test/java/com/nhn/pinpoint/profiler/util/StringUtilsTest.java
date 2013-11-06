package com.nhn.pinpoint.profiler.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class StringUtilsTest {
    @Test
    public void testDrop1() throws Exception {
        String string = "abc";
        String drop = StringUtils.drop(string, 1);
        Assert.assertEquals("a...(3)", drop);
    }

    @Test
    public void testDrop2() throws Exception {
        String string = "abc";
        String drop = StringUtils.drop(string, 5);
        Assert.assertEquals("abc", drop);
    }

    @Test
    public void testDrop3() throws Exception {
        String string = "abc";
        String drop = StringUtils.drop(string, 3);
        Assert.assertEquals("abc", drop);
    }

    @Test
    public void testDrop4() throws Exception {
        String string = "abc";
        String drop = StringUtils.drop(string, 0);
        Assert.assertEquals("...(3)", drop);

    }

    @Test
    public void testDropNegative() throws Exception {
        String string = "abc";
        try {
            StringUtils.drop(string, -1);
            Assert.fail();
        } catch (Exception e) {
        }
    }
}
