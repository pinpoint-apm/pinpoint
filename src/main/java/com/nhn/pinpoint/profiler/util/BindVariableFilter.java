package com.nhn.pinpoint.profiler.util;

import java.lang.reflect.Method;

public interface BindVariableFilter {
    boolean filter(Method method);
}
