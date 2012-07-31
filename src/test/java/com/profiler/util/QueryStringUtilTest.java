package com.profiler.util;

import org.junit.Assert;
import org.junit.Test;

public class QueryStringUtilTest {
    @Test
    public void testRemoveAllMultiSpace() throws Exception {
        String s = QueryStringUtil.removeAllMultiSpace("a   b");

        Assert.assertEquals("a b", s);
    }
}
