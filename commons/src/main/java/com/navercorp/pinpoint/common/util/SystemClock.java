package com.navercorp.pinpoint.common.util;

/**
 * @author emeroad
 */
public final class SystemClock implements Clock {

    public static final Clock INSTANCE = new SystemClock();

    private SystemClock() {
    }

    @Override
    public final long getTime() {
        return System.currentTimeMillis();
    }
}
