package com.navercorp.pinpoint.common.server.bo.codec.stat.strategy;

final class MathUtils {
    private MathUtils() {
    }

    static int min(int a, int b, int c, int d) {
        int min1 = Math.min(a, b);
        int min2 = Math.min(c, d);
        return Math.min(min1, min2);
    }
}
