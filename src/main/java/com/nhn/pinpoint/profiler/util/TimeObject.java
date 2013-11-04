package com.nhn.pinpoint.profiler.util;

/**
 * @author emeroad
 */
public class TimeObject {
    private long cancelTime;
    private long sendTime;

    public void markCancelTime() {
        cancelTime = System.currentTimeMillis();
    }

    public long getCancelTime() {
        return cancelTime;
    }

    public void markSendTime() {
        this.sendTime = System.currentTimeMillis();
    }

    public long getSendTime() {
        return System.currentTimeMillis() - this.sendTime;
    }
}
