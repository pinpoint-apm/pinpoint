package com.nhn.pinpoint.profiler.interceptor.bci;

import javassist.CtMethod;

/**
 *
 */
public interface MethodFilter {
    boolean filter(CtMethod ctMethod);

}
