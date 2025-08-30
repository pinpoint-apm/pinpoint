package com.navercorp.pinpoint.common.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArrayCacheTest {

    private void puts(ArrayCache<String, Integer> cache, int times) {
        for (int i = 0; i < times; i++) {
            cache.put(String.valueOf(i), i);
        }
    }

    @Test
    public void sizeTest() {
        ArrayCache<String, Integer> cacheSize5 = new ArrayCache<>(5);
        puts(cacheSize5, 6);
        Assertions.assertThat(cacheSize5.get(String.valueOf(4))).isNotNull();
        Assertions.assertThat(cacheSize5.get(String.valueOf(5))).isNull();
    }


    @Test
    public void cacheNoEvictionTest() {
        ArrayCache<String, Integer> cacheSize4 = new ArrayCache<>(4);
        puts(cacheSize4, 8);
        Assertions.assertThat(cacheSize4.get(String.valueOf(0))).isNotNull();
        Assertions.assertThat(cacheSize4.get(String.valueOf(1))).isNotNull();
        Assertions.assertThat(cacheSize4.get(String.valueOf(2))).isNotNull();
        Assertions.assertThat(cacheSize4.get(String.valueOf(3))).isNotNull();
    }

    @Test
    public void cacheUpdateTest() {
        ArrayCache<String, Integer> cacheSize4 = new ArrayCache<>(4);
        cacheSize4.put(String.valueOf(0), 1);
        Assertions.assertThat(cacheSize4.get(String.valueOf(0))).isNotNull();

        cacheSize4.put(String.valueOf(0), 0);
        Assertions.assertThat(cacheSize4.get(String.valueOf(0))).isNotNull();
        Assertions.assertThat(cacheSize4.get(String.valueOf(0))).isEqualTo(0);
    }
}
