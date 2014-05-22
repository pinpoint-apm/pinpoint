package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.util.jdk.ThreadLocalRandom;

import java.util.Random;

/**
 * @author emeroad
 */
public class SpanId {

    public static final long NULL = -1;

//    private static final Random seed = new Random();

    public static long newSpanId() {
        // thread 마다 가능한 겹치지 않는 값이 생성되면 문제 없으므로 ThreadLocalRandom으로 변경함.
        final Random random = getRandom();

        return createSpanId(random);
    }

    // 이거 구현 바꾸면 다른 random 사용이 가능함.
    private static Random getRandom() {
        return ThreadLocalRandom.current();
    }

    private static long createSpanId(Random seed) {
        long id = seed.nextLong();
        while (id == NULL) {
            id = seed.nextLong();
        }
        return id;
    }

    public static long nextSpanID(long spanId, long parentSpanId) {
        final Random seed = getRandom();

        long newId = createSpanId(seed);
        while (newId == spanId || newId == parentSpanId) {
            newId = createSpanId(seed);
        }
        return newId;
    }
}
