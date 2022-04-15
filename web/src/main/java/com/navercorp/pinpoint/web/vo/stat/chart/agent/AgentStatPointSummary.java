package com.navercorp.pinpoint.web.vo.stat.chart.agent;


import org.apache.commons.math3.util.Precision;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;

public class AgentStatPointSummary {

    public static AgentStatPoint<Double> doubleSummary(long timestamp, List<Double> values, int avgScale) {
        Objects.requireNonNull(values, "values");

        DoubleSummaryStatistics stats = values.stream()
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
        double average = round(stats.getAverage(), avgScale);
        return new AgentStatPoint<>(timestamp, stats.getMin(), stats.getMax(), average, stats.getSum());
    }

    public static AgentStatPoint<Double> doubleSummaryWithAllScale(long timestamp, List<Double> values, int allScale) {
        Objects.requireNonNull(values, "values");

        DoubleSummaryStatistics stats = values.stream()
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
        double min = round(stats.getMin(), allScale);
        double max = round(stats.getMax(), allScale);
        double average = round(stats.getAverage(), allScale);
        double sum = stats.getSum();
        return new AgentStatPoint<>(timestamp, min, max, average, sum);
    }

    public static AgentStatPoint<Long> longSummary(long timestamp, List<Long> values) {
        Objects.requireNonNull(values, "values");

        LongSummaryStatistics stats = values.stream()
                .mapToLong(Long::longValue)
                .summaryStatistics();
        return new AgentStatPoint<>(timestamp, stats.getMin(), stats.getMax(), stats.getAverage(), stats.getSum());
    }

    public static AgentStatPoint<Long> longSummary(long timestamp, List<Long> values, int avgScale) {
        Objects.requireNonNull(values, "values");

        LongSummaryStatistics stats = values.stream()
                .mapToLong(Long::longValue)
                .summaryStatistics();
        double average = round(stats.getAverage(), avgScale);
        return new AgentStatPoint<>(timestamp, stats.getMin(), stats.getMax(), average, stats.getSum());
    }

    public static AgentStatPoint<Integer> intSummary(long timestamp, List<Integer> values) {
        Objects.requireNonNull(values, "values");

        IntSummaryStatistics stats = values.stream()
                .mapToInt(Integer::intValue)
                .summaryStatistics();
        return new AgentStatPoint<>(timestamp, stats.getMin(), stats.getMax(), stats.getAverage(), (int) stats.getSum());
    }

    public static AgentStatPoint<Integer> intSummary(long timestamp, List<Integer> values, int avgScale) {
        Objects.requireNonNull(values, "values");

        IntSummaryStatistics stats = values.stream()
                .mapToInt(Integer::intValue)
                .summaryStatistics();
        double average = round(stats.getAverage(), avgScale);
        return new AgentStatPoint<>(timestamp, stats.getMin(), stats.getMax(), average, (int) stats.getSum());
    }

    private static double round(double value, int scale) {
        return Precision.round(value, scale);
    }
}
