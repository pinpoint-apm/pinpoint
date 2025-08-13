package com.navercorp.pinpoint.common.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class RingMapCacheTest {

    private void puts(RingMapCache<String, Integer> cacheSize8, int times) {
        for (int i = 0; i < times; i++) {
            cacheSize8.putIfAbsent(String.valueOf(i), i);
        }
    }

    @Test
    public void sizeTest() {
        RingMapCache<String, Integer> cacheSize8 = new RingMapCache<>(5);
        puts(cacheSize8, 8);
        Assertions.assertThat(cacheSize8.get(String.valueOf(0))).isNotNull();
    }


    @Test
    public void cacheEvictionTest() {
        RingMapCache<String, Integer> cacheSize4 = new RingMapCache<>(4);
        puts(cacheSize4, 5);
        Assertions.assertThat(cacheSize4.get(String.valueOf(0))).isNull();
    }

    @Test
    public void cacheFifoTest() {
        RingMapCache<String, Integer> cacheSize4 = new RingMapCache<>(4);
        puts(cacheSize4, 100);

        Assertions.assertThat(cacheSize4.get(String.valueOf(96))).isNotNull();
        Assertions.assertThat(cacheSize4.get(String.valueOf(97))).isNotNull();
        Assertions.assertThat(cacheSize4.get(String.valueOf(98))).isNotNull();
        Assertions.assertThat(cacheSize4.get(String.valueOf(99))).isNotNull();
    }
}
