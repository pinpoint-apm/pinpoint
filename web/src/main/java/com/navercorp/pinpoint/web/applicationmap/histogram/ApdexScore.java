package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * <a href="https://en.wikipedia.org/wiki/Apdex">https://en.wikipedia.org/wiki/Apdex</a>
 */
public class ApdexScore {

    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private final double apdexScore;

    public static double toDoubleFromHistogram(Histogram histogram) {
        Objects.requireNonNull(histogram, "histogram");
        final long satisfiedCount = histogram.getFastCount();
        final long toleratingCount = histogram.getNormalCount();
        final long totalCount = histogram.getTotalCount();

        return calculateApdexScore(satisfiedCount, toleratingCount, totalCount);
    }

    public static ApdexScore newApdexScore(Histogram histogram) {
        Objects.requireNonNull(histogram, "histogram");
        final long satisfiedCount = histogram.getFastCount();
        final long toleratingCount = histogram.getNormalCount();
        final long totalCount = histogram.getTotalCount();

        return new ApdexScore(satisfiedCount, toleratingCount, totalCount);
    }

    public ApdexScore(long satisfiedCount, long toleratingCount, long totalSamples) {
        this.apdexScore = calculateApdexScore(satisfiedCount, toleratingCount, totalSamples);
    }

    private static double calculateApdexScore(long satisfiedCount, long toleratingCount, long totalSamples) {
        // divide by zero
        if (totalSamples == 0) {
            return 0;
        }
        BigDecimal satisfied = BigDecimal.valueOf(satisfiedCount);
        BigDecimal tolerating = BigDecimal.valueOf(toleratingCount);
        BigDecimal total = BigDecimal.valueOf(totalSamples);


        BigDecimal toleratingScore = tolerating.divide(TWO, RoundingMode.FLOOR);
        BigDecimal numerator = satisfied.add(toleratingScore);
        BigDecimal score = numerator.divide(total, 3, RoundingMode.FLOOR);

        return score.doubleValue();
    }

    @JsonProperty
    public double getApdexScore() {
        return this.apdexScore;
    }
}