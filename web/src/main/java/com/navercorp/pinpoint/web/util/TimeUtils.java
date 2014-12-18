package com.navercorp.pinpoint.web.util;

/**
 *
 */
public class TimeUtils {
    private TimeUtils() {
    }

    public static long getDelayLastTime() {
        return System.currentTimeMillis() - 3000;
    }
}
