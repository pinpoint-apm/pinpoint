package com.profiler.context;

import java.util.Random;

public class SpanID {

    public static final long ROOT_SPAN_ID = 0;
    public static final long NULL = -1;

    private static Random seed = new Random();

    public static long newSpanID() {
        long id = seed.nextLong();
        if (id == NULL) {
            return newSpanID();
        }
        return id;
    }
}
