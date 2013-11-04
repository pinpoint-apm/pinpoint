package com.nhn.pinpoint.profiler.sampler;

/**
 * @author emeroad
 */
public class FalseSampler implements Sampler {
    @Override
    public boolean isSampling() {
        return false;
    }
}
