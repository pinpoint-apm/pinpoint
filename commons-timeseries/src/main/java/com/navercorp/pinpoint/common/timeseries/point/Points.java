package com.navercorp.pinpoint.common.timeseries.point;

public final class Points {

    private Points() {
    }

    public static DataPoint<Long> of(long timestamp, long value) {
        return ofLong(timestamp, value);
    }

    public static DataPoint<Long> ofLong(long timestamp, long value) {
        return new LongDataPoint(timestamp, value);
    }

    public static DataPoint<Double> of(long timestamp, double value) {
        return ofDouble(timestamp, value);
    }

    public static DataPoint<Double> ofDouble(long timestamp, double value) {
        return new DoubleDataPoint(timestamp, value);
    }

    public static double asDouble(DataPoint<Double> point) {
        if (point instanceof DoubleDataPoint p) {
            return p.getDoubleValue();
        }
        throw new IllegalArgumentException("Unsupported DataPoint");
    }

    public static long asLong(DataPoint<Long> point) {
        if (point instanceof LongDataPoint p) {
            return p.getLongValue();
        }
        throw new IllegalArgumentException("Unsupported DataPoint");
    }

}
