package com.profiler.context;

import java.util.Random;

public class SpanID {

    public static final long NULL = -1;

    private static Random seed = new Random();

    public static long newSpanID() {
        long id = seed.nextLong();
        if (id == NULL) {
            return newSpanID();
        }
        return id;
    }

    public static long nextSpanID(long parentId) {
        long newId = newSpanID();
        if (newId == parentId) {
            return nextSpanID(parentId);
        }
        return newId;
    }
}
