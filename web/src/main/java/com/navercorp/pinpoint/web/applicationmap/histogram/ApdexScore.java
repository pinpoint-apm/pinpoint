package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class ApdexScore {
    private final double apdexScore;

    public static ApdexScore newApdexScore(Histogram histogram) {
        Objects.requireNonNull(histogram, "histogram");
        final long satisfiedCount = histogram.getFastCount();
        final long toleratingCount = histogram.getNormalCount();
        final long totalCount = histogram.getTotalCount();

        return new ApdexScore(satisfiedCount, toleratingCount, totalCount);
    }

    public ApdexScore(long satisfiedCount, long toleratingCount, long totalCount) {
        this.apdexScore = calculateApdexScore(satisfiedCount, toleratingCount, totalCount);
    }

    private double calculateApdexScore(long satisfiedCount, long toleratingCount, long totalCount) {
        BigDecimal satisfied = new BigDecimal(satisfiedCount);
        BigDecimal tolerating = new BigDecimal(toleratingCount).multiply(BigDecimal.valueOf(0.5));
        BigDecimal total = new BigDecimal(totalCount);
        BigDecimal numerator = satisfied.add(tolerating);
        BigDecimal score = numerator.divide(total, 3, RoundingMode.FLOOR);
        return score.doubleValue();
    }

    @JsonProperty
    public double getApdexScore() {
        return this.apdexScore;
    }
}