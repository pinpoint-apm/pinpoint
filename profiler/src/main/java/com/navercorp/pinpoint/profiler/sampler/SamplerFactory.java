package com.navercorp.pinpoint.profiler.sampler;


import com.navercorp.pinpoint.bootstrap.sampler.Sampler;

public interface SamplerFactory {
    Sampler createSampler();
}
