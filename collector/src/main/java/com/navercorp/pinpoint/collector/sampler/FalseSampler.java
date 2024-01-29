package com.navercorp.pinpoint.collector.sampler;

public class FalseSampler implements Sampler<Object> {

    public static Sampler INSTANCE = new FalseSampler();

    @Override
    public boolean isSampling(Object target) {
        return false;
    }
}
