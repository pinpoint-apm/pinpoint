package com.nhn.pinpoint.common.util;

/**
 * @author emeroad
 */
public final class MathUtils {
    private MathUtils() {
    }

    public static int fastAbs(final int value) {
        return value & Integer.MAX_VALUE;
    }

    public static long roundToNearestMultipleOf(final long num, final long multipleOf) {
        if (num < 0) {
            throw new IllegalArgumentException("num cannot be negative");
        }
        if (multipleOf < 1) {
            throw new IllegalArgumentException("cannot round to nearest multiple of values less than 1");
        }
        if (num < multipleOf) {
            return multipleOf;
        }
        if ((num % multipleOf) >= (multipleOf / 2.0)) {
            return (num + multipleOf) - (num % multipleOf);
        } else {
            return num - (num % multipleOf);
        }
    }
}
