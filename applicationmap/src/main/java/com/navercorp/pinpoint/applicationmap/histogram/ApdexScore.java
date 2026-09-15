package com.navercorp.pinpoint.applicationmap.histogram;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Objects;

/**
 * <a href="https://en.wikipedia.org/wiki/Apdex">https://en.wikipedia.org/wiki/Apdex</a>
 */
public class ApdexScore {

    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private final double apdexScore;

    private final ApdexFormula apdexFormula;

    public record ApdexFormula(long satisfiedCount, long toleratingCount, long totalSamples) {
    }

    public static double calculateApdexScore(Histogram histogram) {
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

    /**
     * Merges the given scores into a single score by summing up their formulas.
     * Used to represent a group of nodes(e.g. a service) as one score.
     */
    public static ApdexScore newApdexScore(Collection<ApdexScore> apdexScores) {
        Objects.requireNonNull(apdexScores, "apdexScores");

        long satisfiedCount = 0;
        long toleratingCount = 0;
        long totalSamples = 0;
        for (ApdexScore apdexScore : apdexScores) {
            final ApdexFormula formula = apdexScore.getApdexFormula();
            satisfiedCount += formula.satisfiedCount();
            toleratingCount += formula.toleratingCount();
            totalSamples += formula.totalSamples();
        }
        return new ApdexScore(satisfiedCount, toleratingCount, totalSamples);
    }

    public ApdexScore(long satisfiedCount, long toleratingCount, long totalSamples) {
        this.apdexScore = calculateApdexScore(satisfiedCount, toleratingCount, totalSamples);
        this.apdexFormula = new ApdexFormula(satisfiedCount, toleratingCount, totalSamples);
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

    @JsonProperty
    public ApdexFormula getApdexFormula() {
        return apdexFormula;
    }

    @Override
    public String toString() {
        return "ApdexScore{" +
                "apdexScore=" + apdexScore +
                ", apdexFormula=" + apdexFormula +
                '}';
    }
}