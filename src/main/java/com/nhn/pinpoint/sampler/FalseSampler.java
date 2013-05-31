package com.nhn.pinpoint.sampler;

/**
 *
 */
public class FalseSampler implements Sampler{
    @Override
    public boolean isSampling() {
        return false;
    }
}
