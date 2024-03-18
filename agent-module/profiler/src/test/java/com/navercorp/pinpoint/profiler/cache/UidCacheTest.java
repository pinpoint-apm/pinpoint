package com.navercorp.pinpoint.profiler.cache;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UidCacheTest {
    @Test
    public void sameValue() {
        UidCache cache1 = newCache();
        UidCache cache2 = newCache();

        Result<byte[]> result1 = cache1.put("test");
        Result<byte[]> result2 = cache2.put("test");

        assertTrue(result1.isNewValue());
        assertTrue(result2.isNewValue());
        assertArrayEquals(result1.getId(), result2.getId());
    }

    @Test
    public void differentValue() {
        UidCache cache = newCache();

        Result<byte[]> result1 = cache.put("test");
        Result<byte[]> result2 = cache.put("different");

        assertTrue(result1.isNewValue());
        assertTrue(result2.isNewValue());
        assertFalse(Arrays.equals(result1.getId(), result2.getId()));
    }

    private UidCache newCache() {
        return new UidCache(1024);
    }
}
