package com.nhn.pinpoint.profiler.sampler;

/**
 *
 */
public class TrueSampler implements Sampler {

    @Override
    public boolean isSampling() {
        return true;
    }
}
