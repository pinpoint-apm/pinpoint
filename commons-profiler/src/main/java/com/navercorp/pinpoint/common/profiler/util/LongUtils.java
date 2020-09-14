package com.navercorp.pinpoint.common.profiler.util;

public final class LongUtils {
    private LongUtils() {
    }

    public static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

}
