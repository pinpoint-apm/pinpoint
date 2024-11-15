package com.navercorp.pinpoint.web.applicationmap.util;

public class TimeoutWatcher {
    public static final long INFINITY_TIME = Long.MAX_VALUE;

    private final long timeoutMillis;
    private final long startTimeMillis;

    public TimeoutWatcher(long timeoutMillis) {
        this.timeoutMillis = timeout(timeoutMillis);
        this.startTimeMillis = currentTimeMillis();
    }

    long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private long timeout(long timeoutMillis) {
        if (timeoutMillis <= 0) {
            return INFINITY_TIME;
        } else {
            return timeoutMillis;
        }
    }

    public long remainingTimeMillis() {
        long elapsedTimeMillis = currentTimeMillis() - this.startTimeMillis;
        if (this.timeoutMillis <= elapsedTimeMillis) {
            return 0;
        }
        return this.timeoutMillis - elapsedTimeMillis;
    }

    public boolean hasRemaining() {
        return remainingTimeMillis() > 0;
    }
}
