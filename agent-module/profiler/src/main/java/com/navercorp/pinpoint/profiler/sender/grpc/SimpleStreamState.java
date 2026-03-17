package com.navercorp.pinpoint.profiler.sender.grpc;

public class SimpleStreamState implements StreamState {
    private final int limitCount;
    private final long limitTime;

    private long failCount = 0;
    private long failureTime = 0;

    private long successCount = 0;
    private long successStartTime = 0;

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
        if (successStartTime == 0) {
            successStartTime = System.currentTimeMillis();
        }
        successCount++;
        failureTime = 0;
        failCount = 0;
    }

    public String report() {
        final long currentSuccessCount = this.successCount;
        final long startTime = this.successStartTime;

        this.successCount = 0;
        this.successStartTime = 0;

        if (startTime == 0 || currentSuccessCount == 0) {
            return "SimpleStreamState.report{successCount=0, tps=0.00}";
        }

        final long elapsedMs = System.currentTimeMillis() - startTime;
        final double elapsedSec = elapsedMs / 1000.0;
        final double tps;
        if (elapsedSec > 0) {
            tps = currentSuccessCount / elapsedSec;
        } else {
            tps = currentSuccessCount;
        }

        return String.format("SimpleStreamState.report{successCount=%d, elapsedMs=%d, tps=%.2f}", currentSuccessCount, elapsedMs, tps);
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
