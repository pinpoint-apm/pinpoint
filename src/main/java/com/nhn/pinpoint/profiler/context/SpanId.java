package com.nhn.pinpoint.profiler.context;

import java.util.Random;

/**
 * @author emeroad
 */
public class SpanId {

    public static final long NULL = -1;

    private static final Random seed = new Random();

    public static long newSpanId() {
        long id = seed.nextLong();
        while (id == NULL) {
            id = seed.nextInt();
        }
        return id;
    }

    public static long nextSpanID(long spanId, long parentSpanId) {
        long newId = newSpanId();
        while (newId == spanId || newId == parentSpanId) {
            newId = newSpanId();
        }
        return newId;
    }
}
