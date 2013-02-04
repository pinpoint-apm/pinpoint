package com.profiler.context;

import java.util.Random;

public class SpanID {

    public static final int NULL = -1;

    private static Random seed = new Random();

    public static int newSpanID() {
        int id = seed.nextInt();
        while (id == NULL) {
            id = seed.nextInt();
        }
        return id;
    }

    public static int nextSpanID(long parentId) {
        int newId = newSpanID();
        while (newId == parentId) {
            newId = newSpanID();
        }
        return newId;
    }
}
