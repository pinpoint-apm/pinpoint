package com.nhn.pinpoint.profiler.interceptor.bci;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;

/**
 * @author emeroad
 */
public interface ByteCodeInstrumentor {


    void checkLibrary(ClassLoader classLoader, String javassistClassName);

    InstrumentClass getClass(String javassistClassName) throws InstrumentException;

    Class<?> defineClass(ClassLoader classLoader, String defineClass, ProtectionDomain protectedDomain) throws InstrumentException;

    Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN) throws InstrumentException;

//    Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN, Object[] params) throws InstrumentException;

    Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN, Object[] params, Class[] paramClazz) throws InstrumentException;
}
