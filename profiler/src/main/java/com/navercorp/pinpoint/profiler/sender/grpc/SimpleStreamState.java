package com.navercorp.pinpoint.profiler.sender.grpc;

public class SimpleStreamState implements StreamState {
    private final int limitCount;
    private final long limitTime;

    private long failCount = 0;
    private long failureTime = 0;

    public SimpleStreamState(int limitCount, long limitTime) {
        this.limitCount = limitCount;
        this.limitTime = limitTime;
    }

    @Override
    public void fail() {
        if (failureTime == 0) {
            failureTime = System.currentTimeMillis();
        }
        failCount++;
    }

    @Override
    public boolean isFailure() {
        final long errorDuration = System.currentTimeMillis() - failureTime;
        return errorDuration > limitTime && failCount > limitCount;
    }

    @Override
    public void success() {
        failureTime = 0;
        failCount = 0;
    }

    @Override
    public String toString() {
        return "SimpleStreamState{" +
                "limitCount=" + limitCount +
                ", limitTime=" + limitTime +
                ", failCount=" + failCount +
                ", failureTime=" + failureTime +
                '}';
    }
}
