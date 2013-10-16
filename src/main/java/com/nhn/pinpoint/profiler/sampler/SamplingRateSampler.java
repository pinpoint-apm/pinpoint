package com.nhn.pinpoint.profiler.sampler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class SamplingRateSampler implements Sampler {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final int samplingRate;

    public SamplingRateSampler(int samplingRate) {
        if (samplingRate <= 0) {
            throw new IllegalArgumentException("Invalid samplingRate " + samplingRate);
        }
        this.samplingRate = samplingRate;
    }



    @Override
    public boolean isSampling() {
        int samplingCount = Math.abs(counter.getAndIncrement());
        int isSampling = samplingCount % samplingRate;
        return isSampling == 0;
    }

    @Override
    public String toString() {
        return "SamplingRateSampler{" +
                    "counter=" + counter +
                    "samplingRate=" + samplingRate +
                '}';
    }
}
