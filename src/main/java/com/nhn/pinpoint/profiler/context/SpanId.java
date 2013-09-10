package com.nhn.pinpoint.profiler.context;

import java.util.Random;

public class SpanId {

    public static final int NULL = -1;

    private static final Random seed = new Random();

    public static int newSpanId() {
        int id = seed.nextInt();
        while (id == NULL) {
            id = seed.nextInt();
        }
        return id;
    }

    public static int nextSpanID(int spanId, int parentSpanId) {
        int newId = newSpanId();
        while (newId == spanId || newId == parentSpanId) {
            newId = newSpanId();
        }
        return newId;
    }
}
