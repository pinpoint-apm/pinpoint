package com.nhn.pinpoint.sampler;

import com.nhn.pinpoint.profiler.sampler.Sampler;

/**
 *
 */
public class FalseSampler implements Sampler {
    @Override
    public boolean isSampling() {
        return false;
    }
}
