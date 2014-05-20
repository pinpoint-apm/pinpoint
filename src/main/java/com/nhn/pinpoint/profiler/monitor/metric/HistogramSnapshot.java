package com.nhn.pinpoint.profiler.monitor.metric;

/**
 * @author emeroad
 */
public class HistogramSnapshot {
    private final long fastCount;
    private final long normalCount;
    private final long slowCount;
    private final long verySlowCount;

    private final long errorCount;

    public HistogramSnapshot(long fastCount, long normalCount, long slowCount, long verySlowCount, long errorCounter) {
        this.fastCount = fastCount;
        this.normalCount = normalCount;
        this.slowCount = slowCount;
        this.verySlowCount = verySlowCount;
        this.errorCount = errorCounter;
    }

    public long getFastCount() {
        return fastCount;
    }

    public long getNormalCount() {
        return normalCount;
    }

    public long getSlowCount() {
        return slowCount;
    }

    public long getVerySlowCount() {
        return verySlowCount;
    }

    public long getErrorCount() {
        return errorCount;
    }
}
