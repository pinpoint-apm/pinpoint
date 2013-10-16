package com.nhn.pinpoint.profiler.sampler;

/**
 *
 */
public class FalseSampler implements Sampler {
    @Override
    public boolean isSampling() {
        return false;
    }
}
