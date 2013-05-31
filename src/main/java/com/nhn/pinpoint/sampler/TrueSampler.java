package com.nhn.pinpoint.sampler;

/**
 *
 */
public class TrueSampler implements Sampler {

    @Override
    public boolean isSampling() {
        return true;
    }
}
