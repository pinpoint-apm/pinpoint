package com.nhn.pinpoint.profiler.modifier.method;

import com.nhn.pinpoint.bootstrap.instrument.Method;
import com.nhn.pinpoint.bootstrap.instrument.MethodFilter;

/**
 * @author emeroad
 */
public class EmptyMethodFilter implements MethodFilter {
    public static final MethodFilter FILTER = new EmptyMethodFilter();

    @Override
    public boolean filter(Method ctMethod) {
        return false;//ctMethod.isEmpty();
    }
}
