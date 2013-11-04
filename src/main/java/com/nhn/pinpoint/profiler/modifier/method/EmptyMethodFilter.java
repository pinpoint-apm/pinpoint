package com.nhn.pinpoint.profiler.modifier.method;

import com.nhn.pinpoint.profiler.interceptor.bci.MethodFilter;
import javassist.CtMethod;

/**
 * @author emeroad
 */
public class EmptyMethodFilter implements MethodFilter {
    public static final MethodFilter FILTER = new EmptyMethodFilter();

    @Override
    public boolean filter(CtMethod ctMethod) {
        return ctMethod.isEmpty();
    }
}
