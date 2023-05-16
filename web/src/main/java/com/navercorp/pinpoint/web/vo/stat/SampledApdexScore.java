package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.UncollectedPointCreatorFactory;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import java.util.Objects;

public class SampledApdexScore implements SampledAgentStatDataPoint {

    public static final Double UNCOLLECTED_SCORE = -1D;
    public static final Point.UncollectedPointCreator<AgentStatPoint<Double>> UNCOLLECTED_POINT_CREATOR = UncollectedPointCreatorFactory.createDoublePointCreator(UNCOLLECTED_SCORE);

    private final AgentStatPoint<Double> apdexScore;

    public SampledApdexScore(AgentStatPoint<Double> apdexScore) {
        this.apdexScore = Objects.requireNonNull(apdexScore, "apdexScore");
    }

    public AgentStatPoint<Double> getApdexScore() {
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
}
