package com.nhn.pinpoint.common.util;

/**
 * 단순한 stopwatch
 * @author emeroad
 */
public class StopWatch {
    private long start;

    public void start() {
        this.start = System.currentTimeMillis();
    }

    public long stop() {
        return System.currentTimeMillis() - this.start;
    }

}
