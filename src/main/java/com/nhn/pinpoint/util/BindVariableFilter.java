package com.nhn.pinpoint.util;

import java.lang.reflect.Method;

public interface BindVariableFilter {
    boolean filter(Method method);
}
