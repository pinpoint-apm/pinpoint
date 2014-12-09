package com.navercorp.pinpoint.profiler.sampler;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;

/**
 * @author emeroad
 */
public class FalseSampler implements Sampler {
    @Override
    public boolean isSampling() {
        return false;
    }

    @Override
    public String toString() {
        // getClass하면 class명이 변경되어 다르게 나올수 있음.
        return "FalseSampler";
    }
}
