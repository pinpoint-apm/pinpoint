package com.navercorp.pinpoint.web.vo.stat.chart.agent;


import org.apache.commons.math3.util.Precision;

import java.util.List;
import java.util.Objects;

public class AgentStatPointSummary {

    public static DoubleAgentStatPoint doubleSummary(long timestamp, List<Double> values, int avgScale) {
        Objects.requireNonNull(values, "values");

        DoubleAgentStatPoint statPoint = new DoubleAgentStatPoint(timestamp) {
            @Override
            public double getAvg() {
                return Precision.round(super.getAvg(), avgScale);
            }
        };
        values.stream()
                .mapToDouble(Double::doubleValue)
                .forEach(statPoint);
        return statPoint;
    }

    public static DoubleAgentStatPoint doubleSummaryWithAllScale(long timestamp, List<Double> values, int allScale) {
        Objects.requireNonNull(values, "values");

        DoubleAgentStatPoint statPoint = new DoubleAgentStatPoint(timestamp) {
            @Override
            public double getMin() {
                return round(super.getMin());
            }

            @Override
            public double getMax() {
                return round(super.getMax());
            }

            @Override
            public double getAvg() {
                return round(super.getAvg());
            }

            private double round(double value) {
                return Precision.round(value, allScale);
            }
        };

        values.stream()
                .mapToDouble(Double::doubleValue)
                .forEach(statPoint);
        return statPoint;
    }

    public static LongAgentStatPoint longSummary(long timestamp, List<Long> values) {
        Objects.requireNonNull(values, "values");

        LongAgentStatPoint statPoint = new LongAgentStatPoint(timestamp);
        values.stream()
                .mapToLong(Long::longValue)
                .forEach(statPoint);
        return statPoint;
    }

    public static LongAgentStatPoint longSummary(long timestamp, List<Long> values, int avgScale) {
        Objects.requireNonNull(values, "values");

        LongAgentStatPoint statPoint = new LongAgentStatPoint(timestamp) {
            @Override
            public double getAvg() {
                return Precision.round(super.getAvg(), avgScale);
            }
        };
        values.stream()
                .mapToLong(Long::longValue)
                .forEach(statPoint);
        return statPoint;
    }

    public static IntAgentStatPoint intSummary(long timestamp, List<Integer> values) {
        Objects.requireNonNull(values, "values");

        IntAgentStatPoint statPoint = new IntAgentStatPoint(timestamp);
        values.stream()
                .mapToInt(Integer::intValue)
                .forEach(statPoint);
        return statPoint;
    }

    public static IntAgentStatPoint intSummary(long timestamp, List<Integer> values, int avgScale) {
        Objects.requireNonNull(values, "values");

        IntAgentStatPoint statPoint = new IntAgentStatPoint(timestamp) {
            public double getAvg() {
                return Precision.round(super.getAvg(), avgScale);
            }
        };
        values.stream()
                .mapToInt(Integer::intValue)
                .forEach(statPoint);

        return statPoint;
    }

}
