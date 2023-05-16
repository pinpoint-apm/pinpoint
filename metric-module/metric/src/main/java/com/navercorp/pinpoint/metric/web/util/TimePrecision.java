package com.navercorp.pinpoint.metric.web.util;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class TimePrecision {
    private final TimeUnit timeUnit;
    private final int timeSize;

    private TimePrecision(TimeUnit timeUnit, Integer timeSize) {
        this.timeUnit = Objects.requireNonNull(timeUnit, "timeUnit");
        this.timeSize = Objects.requireNonNull(timeSize, "timeSize");
    }

    public static TimePrecision newTimePrecision(TimeUnit timeUnit, Integer timeSize) {
        final TimePrecision timePrecision = new TimePrecision(timeUnit, timeSize);
        validate(timePrecision);
        return timePrecision;
    }

    public static void validate(TimePrecision timePrecision) {
        if (timePrecision.timeSize < 1) {
            throw new IllegalArgumentException("invalid timePrecision:" + timePrecision);
        }
    }

    public String getTimeUnit() {
        return timeUnit.name();
    }

    public int getTimeSize() {
        return timeSize;
    }

    public long getInterval() {
        return timeUnit.toMillis(timeSize);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimePrecision{");
        sb.append("timeUnit=").append(timeUnit.name());
        sb.append(", timeSize=").append(timeSize);
        sb.append('}');
        return sb.toString();
    }
}
