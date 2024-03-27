package com.navercorp.pinpoint.collector.sampler;

public class TrueSampler implements Sampler<Object> {

    @SuppressWarnings("rawtypes")
    public static final Sampler INSTANCE = new TrueSampler();

    @SuppressWarnings("unchecked")
    public static <T> Sampler<T> instance() {
        return INSTANCE;
    }

    private TrueSampler() {
    }

    @Override
    public boolean isSampling(Object target) {
        return true;
    }
}
