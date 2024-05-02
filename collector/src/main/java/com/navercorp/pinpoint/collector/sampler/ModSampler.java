package com.navercorp.pinpoint.collector.sampler;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.Objects;
import java.util.function.ToLongFunction;

public class ModSampler<T> implements Sampler<T> {
    private final long divisor;
    private final ToLongFunction<T> function;

    public ModSampler(long samplingRate, ToLongFunction<T> function) {
        Assert.isTrue(samplingRate > 0, "must be `samplingRate > 0`");
        this.divisor = samplingRate;
        this.function = Objects.requireNonNull(function, "function");
    }

    @Override
    public boolean isSampling(T target) {
        long dividend = function.applyAsLong(target) % divisor;
        return (dividend == 0);
    }
}
