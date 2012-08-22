package com.profiler.interceptor.bci;

import javassist.ClassPool;


public interface ByteCodeInstrumentor {

    // 임시로 만들자.
    ClassPool getClassPool();

    void checkLibrary(ClassLoader classLoader, String javassistClassName);

     InstrumentClass getClass(String javassistClassName);
}
