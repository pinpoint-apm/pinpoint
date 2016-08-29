package com.navercorp.pinpoint.profiler.instrument.mock;

/**
 * @author jaehong.kim
 */
public class ThisUninitClass {
    public ThisUninitClass(ThisUninitClass t) {
        this();
    }

    public ThisUninitClass() {}
}
