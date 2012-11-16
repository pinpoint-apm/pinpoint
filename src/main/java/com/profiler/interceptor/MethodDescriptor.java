package com.profiler.interceptor;

import java.lang.reflect.Method;

/**
 *
 */
public interface MethodDescriptor {
    String getMethodName();

    String getClassName();

    String getSimpleClassName();

    String[] getParameterTypes();

    String[] getSimpleParameterTypes();

    String[] getParameterVariableName();


    String getParameterDescriptor();

    String getSimpleParameterDescriptor();

    int getLineNumber();
}
