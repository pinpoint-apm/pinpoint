package com.nhn.pinpoint.profiler.sampler;

import com.nhn.pinpoint.profiler.sampler.Sampler;

/**
 *
 */
public class TrueSampler implements Sampler {

    @Override
    public boolean isSampling() {
        return true;
    }
}
