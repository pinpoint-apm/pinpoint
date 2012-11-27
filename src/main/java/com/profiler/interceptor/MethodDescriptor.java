package com.profiler.interceptor;

import java.lang.reflect.Method;

/**
 *
 */
public interface MethodDescriptor {
    String getMethodName();

    String getClassName();


    String[] getParameterTypes();

    String[] getParameterVariableName();


    String getParameterDescriptor();


    int getLineNumber();
}
