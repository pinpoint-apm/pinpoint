package com.navercorp.pinpoint.collector.sampler;

public interface SamplingFunction<V> {
    long sample(V source);
}
