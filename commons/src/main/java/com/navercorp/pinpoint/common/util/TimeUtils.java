package com.nhn.pinpoint.common.util;

/**
 * @author emeroad
 */
public final class TimeUtils {

    public static long reverseTimeMillis(long currentTimeMillis) {
        return Long.MAX_VALUE - currentTimeMillis;
    }

    public static long reverseCurrentTimeMillis() {
        return reverseTimeMillis(System.currentTimeMillis());
    }

    public static long recoveryTimeMillis(long reverseCurrentTimeMillis) {
        return Long.MAX_VALUE - reverseCurrentTimeMillis;
    }
}
