package com.navercorp.pinpoint.profiler.util;

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.profiler.util.QueryStringUtil;

/**
 * @author emeroad
 */
public class QueryStringUtilTest {
    @Test
    public void testRemoveAllMultiSpace() throws Exception {
        String s = QueryStringUtil.removeAllMultiSpace("a   b");

        Assert.assertEquals("a b", s);
    }
}
