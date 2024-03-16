package com.navercorp.pinpoint.profiler.sender.grpc;

import io.grpc.internal.ExponentialBackoffPolicy;

public class ExpireStreamState implements StreamState {

    private long checkTime;
    private final long maxAge;
    private final ExponentialBackoffPolicy policy = new ExponentialBackoffPolicy();
    private int failCount;

    public ExpireStreamState(long maxAge) {
        this.checkTime = currentTimeMillis();
        this.maxAge = maxAge;
    }


    long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public void fail() {
        this.failCount++;
    }

    @Override
    public long getFailCount() {
        return this.failCount;
    }

    @Override
    public boolean isFailure() {
        final long currentTimeMillis = currentTimeMillis();
        final long expireTime = checkTime + maxAge;
        if (currentTimeMillis > expireTime) {
            checkTime = currentTimeMillis;
            return true;
        }

        return false;
    }

    @Override
    public void success() {
        this.failCount = 0;
    }

    @Override
    public String toString() {
        return "SimpleStreamState{";

    }
}
