package com.profiler.common.util;

/**
 *
 */
public class TimeUtils {

    public static long reverseCurrentTimeMillis(long currentTimeMillis) {
        return Long.MAX_VALUE - currentTimeMillis;
    }

    public static long reverseCurrentTimeMillis() {
        return reverseCurrentTimeMillis(System.currentTimeMillis());
    }

    public static long recoveryCurrentTimeMillis(long reverseCurrentTimeMillis) {
        return Long.MAX_VALUE - reverseCurrentTimeMillis;
    }
}
