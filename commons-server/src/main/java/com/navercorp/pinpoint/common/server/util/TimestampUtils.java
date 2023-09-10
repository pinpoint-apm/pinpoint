package com.navercorp.pinpoint.common.server.util;

public class TimestampUtils {

    private TimestampUtils() {
    }

    private static final long TIMESLOT_SIZE = 3_600_000; // 1hour

    private static final TimeSlot timeSlot = new DefaultTimeSlot(TIMESLOT_SIZE);

    public static long reverseRoundedCurrentTimeMillis() {
        return reverseRoundedTimeMillis(System.currentTimeMillis());
    }

    public static long reverseRoundedTimeMillis(long timeMillis) {
        return Long.MAX_VALUE - timeSlot.getTimeSlot(timeMillis);
    }

    public static long getTimeslotSize() {
        return TIMESLOT_SIZE;
    }
}
