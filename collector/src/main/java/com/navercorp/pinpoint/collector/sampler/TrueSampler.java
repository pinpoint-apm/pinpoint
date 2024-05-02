package com.navercorp.pinpoint.collector.sampler;

public class TrueSampler<T> implements Sampler<T> {

    public static final Sampler<?> INSTANCE = new TrueSampler<>();

    @SuppressWarnings("unchecked")
    public static <T> Sampler<T> instance() {
        return (Sampler<T>) INSTANCE;
    }

    private TrueSampler() {
    }

    @Override
    public boolean isSampling(T target) {
        return true;
    }
}
