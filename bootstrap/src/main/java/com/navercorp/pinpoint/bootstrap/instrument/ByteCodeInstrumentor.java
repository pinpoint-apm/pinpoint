package com.nhn.pinpoint.bootstrap.instrument;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;

/**
 * @author emeroad
 */
public interface ByteCodeInstrumentor {

    @Deprecated
    void checkLibrary(ClassLoader classLoader, String javassistClassName);

    @Deprecated
    InstrumentClass getClass(String className) throws InstrumentException;
    
    InstrumentClass getClass(ClassLoader classLoader, String className, byte[] classFileBuffer) throws InstrumentException;

    Scope getScope(String scopeName);

    Class<?> defineClass(ClassLoader classLoader, String defineClass, ProtectionDomain protectedDomain) throws InstrumentException;

    Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN) throws InstrumentException;

//    Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN, Object[] params) throws InstrumentException;

    Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN, Object[] params, Class[] paramClazz) throws InstrumentException;
}
