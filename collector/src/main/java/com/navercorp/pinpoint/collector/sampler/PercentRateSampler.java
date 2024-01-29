package com.navercorp.pinpoint.collector.sampler;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.Objects;
import java.util.function.Function;

public class PercentRateSampler<T> implements Sampler<T> {
    public static final long MULTIPLIER = 100;
    public static final long MAX = 100 * MULTIPLIER;

    private final long samplingRate;
    private final Function<T, ? extends Number> function;

    public static long parseSamplingRateString(String samplingRateString) {
        return (long) (Double.parseDouble(samplingRateString) * MULTIPLIER);
    }

    public PercentRateSampler(long samplingRate, Function<T, ? extends Number> function) {
        Assert.isTrue(samplingRate >= 0, "must be `sampling percentage >= 0`");
        Assert.isTrue(samplingRate <= MAX, "must be `sampling percentage <= 100`");
        this.samplingRate = samplingRate;
        this.function = Objects.requireNonNull(function, "function");
    }

    public PercentRateSampler(String samplingRateString, Function<T, ? extends Number> function) {
        this(parseSamplingRateString(samplingRateString), function);
    }

    @Override
    public boolean isSampling(T target) {
        long dividend = function.apply(target).longValue() % MAX;
        return (dividend < samplingRate);
    }
}
