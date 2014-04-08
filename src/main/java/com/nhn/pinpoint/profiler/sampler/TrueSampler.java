package com.nhn.pinpoint.profiler.sampler;

import com.nhn.pinpoint.bootstrap.sampler.Sampler;

/**
 * @author emeroad
 */
public class TrueSampler implements Sampler {

    @Override
    public boolean isSampling() {
        return true;
    }
}
