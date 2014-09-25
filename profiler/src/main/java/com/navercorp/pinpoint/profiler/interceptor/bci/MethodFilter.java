package com.nhn.pinpoint.profiler.interceptor.bci;

import javassist.CtMethod;

/**
 * @author emeroad
 */
public interface MethodFilter {
    boolean filter(CtMethod ctMethod);

}
