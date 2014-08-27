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

}
