package com.navercorp.pinpoint.collector.sampler;

public interface Sampler<T> {
    boolean isSampling(T target);
}
