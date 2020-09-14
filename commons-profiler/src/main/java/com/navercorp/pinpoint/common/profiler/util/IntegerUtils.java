package com.navercorp.pinpoint.common.profiler.util;

public final class IntegerUtils {
    private IntegerUtils() {
    }

    public static int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
