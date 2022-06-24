package com.navercorp.pinpoint.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BiHashMapTest {
    @Test
    public void put() {
        BiHashMap<Integer, String> map = new BiHashMap<>();
        map.put(1, "a");
        map.put(1, "b");
        Assertions.assertEquals("b", map.get(1));
        map.put(2, "b");
        Assertions.assertEquals(2, map.reverseGet("b").intValue());
    }

}