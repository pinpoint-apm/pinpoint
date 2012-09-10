package com.profiler.util;

import java.lang.reflect.Method;

public interface BindVariableFilter {
    boolean filter(Method method);
}
