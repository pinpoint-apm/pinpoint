package com.navercorp.pinpoint.common.server.util.time;

import java.time.Instant;

public final class DateTimeUtils {
    private DateTimeUtils() {
    }

    /**
     * Returns the current time in milliseconds.
     * precision : milliseconds
     * @return the current time in milliseconds
     */
    public static Instant epochMilli() {
        return Instant.ofEpochMilli(System.currentTimeMillis());
    }

}
