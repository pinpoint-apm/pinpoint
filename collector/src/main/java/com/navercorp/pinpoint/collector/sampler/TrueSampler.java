package com.navercorp.pinpoint.collector.sampler;

public class TrueSampler implements Sampler<Object> {

    public static Sampler INSTANCE = new TrueSampler();

    @Override
    public boolean isSampling(Object target) {
        return true;
    }
}
