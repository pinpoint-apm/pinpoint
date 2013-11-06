package com.nhn.pinpoint.web.util;

/**
 * @author emeroad
 */
public final class LimitUtils {
    public static final int MAX = 10000;

    public static int checkRange(final int limit) {
        if (limit < 0) {
            return 0;
        }
        if (limit > MAX) {
            return MAX;
        }
        return limit;
    }
}
