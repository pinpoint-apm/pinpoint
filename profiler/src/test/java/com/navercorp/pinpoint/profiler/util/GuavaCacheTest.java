package com.nhn.pinpoint.profiler.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class GuavaCacheTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void test() {
        CacheBuilder builder = CacheBuilder.newBuilder();
        builder.concurrencyLevel(8);
        builder.maximumSize(1);
        builder.initialCapacity(1);
        Cache<String, Object> cache = builder.build();

        cache.put("test1", "1");
        logger.debug("{}", cache.size());
        cache.put("test3", "2");
        logger.debug("{}", cache.size());

    }
}
