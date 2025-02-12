package com.navercorp.pinpoint.profiler.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UidCacheTest {
    static int LENGTH_LIMIT = 100;

    UidCache sut = new UidCache(100, new UidGenerator.Murmur(), LENGTH_LIMIT);

    static String veryLongString() {
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < LENGTH_LIMIT + 1; i++) {
            a.append("a");
        }
        return a.toString();
    }

    @Test
    void bypassCache() {
        // given
        String veryLongString = veryLongString();

        // when
        Result<byte[]> result1 = sut.put(veryLongString);
        Result<byte[]> result2 = sut.put(veryLongString);

        // then
        assertArrayEquals(result1.getId(), result2.getId());
        assertTrue(result1.isNewValue());
        assertTrue(result2.isNewValue());
    }
}