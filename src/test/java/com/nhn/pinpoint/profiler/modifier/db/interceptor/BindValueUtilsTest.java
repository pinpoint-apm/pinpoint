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
        Assert.assertEquals("...(2)", result);
    }

    @Test
    public void testBindValueToString_limit2() throws Exception {
        String[] bindValue = {"a", "b"};
        String result = BindValueUtils.bindValueToString(bindValue, 1);
        Assert.assertEquals("a, ...(2)", result);
    }

    @Test
    public void testBindValueToString_limit3() throws Exception {
        String[] bindValue = {"abc", "b"};
        String result = BindValueUtils.bindValueToString(bindValue, 1);
        Assert.assertEquals("a...(3), ...(2)", result);
    }

    @Test
    public void testBindValueToString_limit4() throws Exception {
        String[] bindValue = {"abc", "b", "c"};
        String result = BindValueUtils.bindValueToString(bindValue, 1);
        Assert.assertEquals("a...(3), ...(3)", result);
    }


    @Test
    public void testBindValueToString_limit5() throws Exception {
        String[] bindValue = {"abc", "b", "c"};
        String result = BindValueUtils.bindValueToString(bindValue, 1024);
        Assert.assertEquals("abc, b, c", result);
    }

    @Test
    public void testBindValueToString_limit6() throws Exception {
        String[] bindValue = {"a", "b", "1234567891012"};
        // limit를 3번째 문자열 길이보다는 작게한다.
        String result = BindValueUtils.bindValueToString(bindValue, 10);
        Assert.assertEquals("a, b, 1234567891...(13)", result);
    }

    @Test
    public void testBindValueToString_limit7() throws Exception {
        String[] bindValue = {"a", "12345678901", "c"};
        // limit를 2번째 문자열 길이보다는 작게한다.
        String result = BindValueUtils.bindValueToString(bindValue, 10);
        Assert.assertEquals("a, 1234567890...(11), ...(3)", result);
    }

    @Test
    public void testBindValueToString_null() throws Exception {
        String result = BindValueUtils.bindValueToString(null, 10);
        Assert.assertEquals("", result);
    }

    @Test
    public void testBindValueToString_native() throws Exception {
        String[] bindValue = {"a", "b"};
        String result = BindValueUtils.bindValueToString(bindValue, -1);
        Assert.assertEquals("...(2)", result);
    }

    @Test
    public void testBindValueToString_singleLargeString() throws Exception {
        String[] bindValue = {"123456"};
        String result = BindValueUtils.bindValueToString(bindValue, 5);
        Assert.assertEquals("12345...(6)", result);
    }

    @Test
    public void testBindValueToString_twoLargeString() throws Exception {
        String[] bindValue = {"123456", "123456"};
        String result = BindValueUtils.bindValueToString(bindValue, 5);
        Assert.assertEquals("12345...(6), ...(2)", result);
    }
}