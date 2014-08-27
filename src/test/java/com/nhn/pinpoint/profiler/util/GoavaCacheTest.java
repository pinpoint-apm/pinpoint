package com.nhn.pinpoint.profiler.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.junit.Test;

/**
 * @author emeroad
 */
public class GoavaCacheTest {
    @Test
    public void test() {
        CacheBuilder builder = CacheBuilder.newBuilder();
        builder.concurrencyLevel(8);
        builder.maximumSize(1);
        builder.initialCapacity(1);
        Cache<String, Object> cache = builder.build();

        cache.put("test1", "1");
        System.out.println(cache.size());
        cache.put("test3", "2");
        System.out.println(cache.size());

    }
}
