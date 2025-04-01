package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import java.util.Objects;

public class SampledApdexScore implements SampledAgentStatDataPoint {

    public static final double UNCOLLECTED_SCORE = -1D;

    private final AgentStatPoint apdexScore;

    public SampledApdexScore(AgentStatPoint apdexScore) {
        this.apdexScore = Objects.requireNonNull(apdexScore, "apdexScore");
    }

    public AgentStatPoint getApdexScore() {
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

    public static AgentStatPoint newPoint(long timestamp) {
        return new AgentStatPoint(timestamp, UNCOLLECTED_SCORE);
    }
}
