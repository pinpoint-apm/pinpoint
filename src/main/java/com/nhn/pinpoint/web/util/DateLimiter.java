package com.nhn.pinpoint.web.util;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class DateLimiter implements Limiter {

    private final long limitDay;
    private final long limitDayMillis;

    public DateLimiter() {
        this(2);
    }

    public DateLimiter(int limitDay) {
        if (limitDay < 0) {
            throw new IllegalArgumentException("limitDay < 0 " + limitDay);
        }
        this.limitDay = limitDay;
        this.limitDayMillis = TimeUnit.DAYS.toMillis((long) limitDay);
    }

    @Override
    public void limit(long from, long to) {
        final long elapsedTime = to - from;
        if (elapsedTime < 0) {
            throw new  IllegalArgumentException("to - from < 0 to:" + to + " from:" + from);
        }
        if (limitDayMillis < elapsedTime) {
            throw new IllegalArgumentException("limitDay");
        }
    }
}
