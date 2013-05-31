package com.nhn.pinpoint.util;

import com.profiler.util.QueryStringUtil;
import org.junit.Assert;
import org.junit.Test;

public class QueryStringUtilTest {
    @Test
    public void testRemoveAllMultiSpace() throws Exception {
        String s = QueryStringUtil.removeAllMultiSpace("a   b");

        Assert.assertEquals("a b", s);
    }
}
