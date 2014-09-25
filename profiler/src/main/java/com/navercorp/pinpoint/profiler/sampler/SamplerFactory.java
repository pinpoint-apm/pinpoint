package com.nhn.pinpoint.profiler.sampler;

import com.nhn.pinpoint.bootstrap.sampler.Sampler;

/**
 * @author emeroad
 */
public class SamplerFactory {
    public Sampler createSampler(boolean sampling, int samplingRate) {
        if (!sampling || samplingRate <= 0) {
            return new FalseSampler();
        }
        if (samplingRate == 1) {
            return new TrueSampler();
        }
        return new SamplingRateSampler(samplingRate);
    }
}
