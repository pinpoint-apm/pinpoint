package com.nhn.pinpoint.profiler.sampler;

/**
 * @author emeroad
 */
public class TrueSampler implements Sampler {

    @Override
    public boolean isSampling() {
        return true;
    }
}
