package com.navercorp.pinpoint.test.util;

import org.junit.Assert;
import org.junit.Test;

public class BiHashMapTest {
    @Test
    public void put() {
        BiHashMap<Integer, String> map = new BiHashMap<>();
        map.put(1, "a");
        map.put(1, "b");
        Assert.assertEquals("b", map.get(1));
        map.put(2, "b");
        Assert.assertEquals(2, map.reverseGet("b").intValue());
    }

}