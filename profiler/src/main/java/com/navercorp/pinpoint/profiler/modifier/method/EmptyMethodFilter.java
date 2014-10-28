package com.nhn.pinpoint.profiler.modifier.method;

import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.instrument.MethodFilter;

/**
 * @author emeroad
 */
public class EmptyMethodFilter implements MethodFilter {
    public static final MethodFilter FILTER = new EmptyMethodFilter();

    @Override
    public boolean filter(MethodInfo ctMethod) {
        return false;//ctMethod.isEmpty();
    }
}
