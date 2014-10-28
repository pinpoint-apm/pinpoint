package com.nhn.pinpoint.profiler.interceptor.bci;

import com.nhn.pinpoint.bootstrap.instrument.Method;
import com.nhn.pinpoint.bootstrap.instrument.MethodFilter;

/**
 * @author emeroad
 */
public class SkipMethodFilter implements MethodFilter {
    public static final MethodFilter FILTER = new SkipMethodFilter();

    @Override
    public boolean filter(Method ctMethod) {
        return false;
    }
}
