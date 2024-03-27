package com.navercorp.pinpoint.collector.sampler;

public class FalseSampler implements Sampler<Object> {

    @SuppressWarnings("rawtypes")
    public static final Sampler INSTANCE = new FalseSampler();

    @SuppressWarnings("unchecked")
    public static <T> Sampler<T> instance() {
        return INSTANCE;
    }

    private FalseSampler() {
    }

    @Override
    public boolean isSampling(Object target) {
        return false;
    }
}
