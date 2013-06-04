package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.profiler.util.QueryStringUtil;
import org.junit.Assert;
import org.junit.Test;

public class QueryStringUtilTest {
    @Test
    public void testRemoveAllMultiSpace() throws Exception {
        String s = QueryStringUtil.removeAllMultiSpace("a   b");

        Assert.assertEquals("a b", s);
    }
}
