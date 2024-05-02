package com.navercorp.pinpoint.collector.sampler;

public class FalseSampler<T> implements Sampler<T> {

    public static final Sampler<?> INSTANCE = new FalseSampler<>();

    @SuppressWarnings("unchecked")
    public static <T> Sampler<T> instance() {
        return (Sampler<T>) INSTANCE;
    }

    private FalseSampler() {
    }

    @Override
    public boolean isSampling(T target) {
        return false;
    }
}
