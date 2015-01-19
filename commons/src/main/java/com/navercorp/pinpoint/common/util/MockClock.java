package com.navercorp.pinpoint.common.util;

/**
 * @author emeroad
 */
public class MockClock implements Clock {

    private long time;

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public long getTime() {
        return time;
    }
}
