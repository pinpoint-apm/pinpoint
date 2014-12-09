package com.navercorp.pinpoint.profiler.modifier.method;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;

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
