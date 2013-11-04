package com.nhn.pinpoint.profiler.modifier.mapping;

import org.junit.Test;

/**
 * @author emeroad
 */
public class MappingTableTest {
    @Test
    public void testLookupMethodDescriptor() throws Exception {
        // 2147483647
        // xxxxxxxx  - yyy  xxxx는 class 명, yyy는 함수명매칭하자. 하자.
        int maxValue = Integer.MAX_VALUE;
        int i = 2147483647 / 100;
        System.out.println(i);


    }
}
