package com.profiler.modifier.mapping;

import com.nhn.pinpoint.common.mapping.ApiUtils;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 */
public class ApiUtilsTest {
    // 2147483647
    // xxxxxxxx  - yyy  xxxx는 class 명, yyy는 함수명매칭하자. 하자.
    @Test
    public void testParseClassId() throws Exception {
        int i = ApiUtils.parseClassId(2147483647);
        Assert.assertEquals(i, 21474836);

    }

    @Test
    public void testParseMethodId() throws Exception {
        int i = ApiUtils.parseMethodId(2147483647);
        Assert.assertEquals(i, 47);
    }
}
