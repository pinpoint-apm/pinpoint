package com.navercorp.pinpoint.collector.sampler;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.Objects;
import java.util.function.Function;

public class ModSampler<T> implements Sampler<T> {
    private final long divisor;
    private final Function<T, ? extends Number> function;

    public ModSampler(long samplingRate, Function<T, ? extends Number> function) {
        Assert.isTrue(samplingRate > 0, "must be `samplingRate > 0`");
        this.divisor = samplingRate;
        this.function = Objects.requireNonNull(function, "function");
    }

    @Override
    public boolean isSampling(T target) {
        long dividend = function.apply(target).longValue() % divisor;
        return (dividend == 0);
    }
}
