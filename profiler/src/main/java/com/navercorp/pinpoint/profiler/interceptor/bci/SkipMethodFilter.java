package com.nhn.pinpoint.profiler.interceptor.bci;

import javassist.CtMethod;

/**
 * @author emeroad
 */
public class SkipMethodFilter implements MethodFilter {
    public static final MethodFilter FILTER = new SkipMethodFilter();

    @Override
    public boolean filter(CtMethod ctMethod) {
        return false;
    }
}
