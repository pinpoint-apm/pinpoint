package com.navercorp.pinpoint.common.server.uid.cache;

public class UidCaffeineCacheProperties {

    protected int initialCapacity = 10;
    protected long maximumWeight = -1L;
    protected long maximumSize = 1000L;

    protected long expireAfterWriteSeconds = 600L;
    protected long expireAfterAccessSeconds = -1L;
    protected long refreshAfterWriteSeconds = -1L;

    protected boolean recordStats;

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public long getMaximumWeight() {
        return maximumWeight;
    }

    public void setMaximumWeight(long maximumWeight) {
        this.maximumWeight = maximumWeight;
    }

    public long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }

    public long getExpireAfterWriteSeconds() {
        return expireAfterWriteSeconds;
    }

    public void setExpireAfterWriteSeconds(long expireAfterWriteSeconds) {
        this.expireAfterWriteSeconds = expireAfterWriteSeconds;
    }

    public long getExpireAfterAccessSeconds() {
        return expireAfterAccessSeconds;
    }

    public void setExpireAfterAccessSeconds(long expireAfterAccessSeconds) {
        this.expireAfterAccessSeconds = expireAfterAccessSeconds;
    }

    public long getRefreshAfterWriteSeconds() {
        return refreshAfterWriteSeconds;
    }

    public void setRefreshAfterWriteSeconds(long refreshAfterWriteSeconds) {
        this.refreshAfterWriteSeconds = refreshAfterWriteSeconds;
    }

    public boolean isRecordStats() {
        return recordStats;
    }

    public void setRecordStats(boolean recordStats) {
        this.recordStats = recordStats;
    }

    @Override
    public String toString() {
        return "UidCaffeineCacheProperties{" +
                "initialCapacity=" + initialCapacity +
                ", maximumWeight=" + maximumWeight +
                ", maximumSize=" + maximumSize +
                ", expireAfterWriteSeconds=" + expireAfterWriteSeconds +
                ", expireAfterAccessSeconds=" + expireAfterAccessSeconds +
                ", refreshAfterWriteSeconds=" + refreshAfterWriteSeconds +
                ", recordStats=" + recordStats +
                '}';
    }
}
