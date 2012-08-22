package com.profiler.interceptor.bci;

import com.profiler.interceptor.Interceptor;
import javassist.ClassPool;

public interface ByteCodeInstrumentor {

    void addInterceptor(String className, String methodName, String[] args, Interceptor interceptor);

    // 임시로 만들자.
    ClassPool getClassPool();

    void checkLibrary(ClassLoader classLoader, String javassistClassName);
}
