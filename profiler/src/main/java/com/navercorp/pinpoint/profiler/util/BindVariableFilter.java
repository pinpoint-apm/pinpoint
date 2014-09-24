package com.nhn.pinpoint.profiler.util;

import java.lang.reflect.Method;

/**
 * @author emeroad
 */
public interface BindVariableFilter {
    boolean filter(Method method);
}
