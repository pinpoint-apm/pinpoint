package com.profiler.interceptor.bci;

import com.profiler.interceptor.Interceptor;

public interface ByteCodeManipulator {
    void addInterceptor(Interceptor interceptor);
}
