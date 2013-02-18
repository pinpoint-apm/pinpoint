package com.profiler.context;

import java.util.Random;

public class SpanID {

    public static final int NULL = -1;

    private static final Random seed = new Random();

    public static int newSpanID() {
        int id = seed.nextInt();
        while (id == NULL) {
            id = seed.nextInt();
        }
        return id;
    }

    public static int nextSpanID(int spanId, int parentSpanId) {
        int newId = newSpanID();
        while (newId == spanId || newId == parentSpanId) {
            newId = newSpanID();
        }
        return newId;
    }
}
