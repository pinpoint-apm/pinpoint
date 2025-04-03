package com.navercorp.pinpoint.metric.common.model.chart;

public abstract class AbstractSystemMetricPoint<Y extends Number> implements SystemMetricPoint<Y> {

    protected final long timestamp;

    public AbstractSystemMetricPoint(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public abstract Y getYVal();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractSystemMetricPoint<?> that)) return false;

        return timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(timestamp);
    }
}
