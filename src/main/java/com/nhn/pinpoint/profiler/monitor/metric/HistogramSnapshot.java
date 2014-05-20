package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public class HistogramSnapshot {
    private final ServiceType serviceType;
    private final long fastCount;
    private final long normalCount;
    private final long slowCount;
    private final long verySlowCount;

    private final long errorCount;

    public HistogramSnapshot(ServiceType serviceType, long fastCount, long normalCount, long slowCount, long verySlowCount, long errorCounter) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.serviceType = serviceType;
        this.fastCount = fastCount;
        this.normalCount = normalCount;
        this.slowCount = slowCount;
        this.verySlowCount = verySlowCount;
        this.errorCount = errorCounter;
    }

    public ServiceType getServiceType() {
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
