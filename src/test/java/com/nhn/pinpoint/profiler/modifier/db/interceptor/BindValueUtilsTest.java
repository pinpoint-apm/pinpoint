package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import junit.framework.Assert;
import org.junit.Test;

public class BindValueUtilsTest {

    @Test
    public void testBindValueToString() throws Exception {
        String[] bindValue = {"a", "b"};
        String result = BindValueUtils.bindValueToString(bindValue);
        Assert.assertEquals("a, b", result);
    }

    @Test
    public void testBindValueToString_limit1() throws Exception {
        String[] bindValue = {"a", "b"};
        String result = BindValueUtils.bindValueToString(bindValue, 0);
        Assert.assertEquals("a", result);
    }

    @Test
    public void testBindValueToString_limit2() throws Exception {
        String[] bindValue = {"a", "b"};
        String result = BindValueUtils.bindValueToString(bindValue, 1);
        Assert.assertEquals("a, b", result);
    }

    @Test
    public void testBindValueToString_limit3() throws Exception {
        String[] bindValue = {"abc", "b"};
        String result = BindValueUtils.bindValueToString(bindValue, 1);
        Assert.assertEquals("abc", result);
    }
}