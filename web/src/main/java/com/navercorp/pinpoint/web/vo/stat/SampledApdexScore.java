package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.common.timeseries.point.Points;

import java.util.Objects;

public class SampledApdexScore implements SampledAgentStatDataPoint {

    public static final double UNCOLLECTED_SCORE = -1D;

    private final DataPoint<Double> apdexScore;

    public SampledApdexScore(DataPoint<Double> apdexScore) {
        this.apdexScore = Objects.requireNonNull(apdexScore, "apdexScore");
    }

    public DataPoint<Double> getApdexScore() {
        return apdexScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledApdexScore that = (SampledApdexScore) o;

        return Objects.equals(apdexScore, that.apdexScore);
    }

    @Override
    public int hashCode() {
        return apdexScore.hashCode();
    }

    @Override
    public String toString() {
        return "SampledApdexScore{" +
                "apdexScore=" + apdexScore +
                '}';
    }

    public static DataPoint<Double> newPoint(long timestamp) {
        return Points.ofDouble(timestamp, UNCOLLECTED_SCORE);
    }
}
