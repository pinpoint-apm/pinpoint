package com.nhn.pinpoint.profiler.monitor.metric;

/**
 * @author emeroad
 */
public class HistogramSnapshot {
    private final short serviceType;
    private final long fastCount;
    private final long normalCount;
    private final long slowCount;
    private final long verySlowCount;

    private final long errorCount;

    public HistogramSnapshot(short serviceType, long fastCount, long normalCount, long slowCount, long verySlowCount, long errorCounter) {

        this.serviceType = serviceType;
        this.fastCount = fastCount;
        this.normalCount = normalCount;
        this.slowCount = slowCount;
        this.verySlowCount = verySlowCount;
        this.errorCount = errorCounter;
    }

    public short getServiceType() {
        return serviceType;
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

    @Override
    public String toString() {
        return "HistogramSnapshot{" +
                "serviceType=" + serviceType +
                "fast=" + fastCount +
                ", normal=" + normalCount +
                ", slow=" + slowCount +
                ", verySlow=" + verySlowCount +
                ", error=" + errorCount +
                '}';
    }
}
