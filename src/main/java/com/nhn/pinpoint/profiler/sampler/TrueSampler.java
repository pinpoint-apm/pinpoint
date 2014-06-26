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

    @Override
    public String toString() {
        // getClass하면 class명이 변경되어 다르게 나올수 있음.
        return "TrueSampler";
    }
}
