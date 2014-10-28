package com.nhn.pinpoint.profiler.interceptor.bci;

import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.instrument.MethodFilter;

/**
 * @author emeroad
 */
public class SkipMethodFilter implements MethodFilter {
    public static final MethodFilter FILTER = new SkipMethodFilter();

    @Override
    public boolean filter(MethodInfo ctMethod) {
        return false;
    }
}
