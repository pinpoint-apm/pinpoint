package com.navercorp.pinpoint.metric.common.model.chart;

public abstract class AbstractSystemMetricPoint<Y extends Number> implements SystemMetricPoint<Y> {

    protected final long x;

    public AbstractSystemMetricPoint(long x) {
        this.x = x;
    }

    @Override
    public long getXVal() {
        return x;
    }

    public abstract Y getYVal();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractSystemMetricPoint<?> that)) return false;

        return x == that.x;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(x);
    }
}
