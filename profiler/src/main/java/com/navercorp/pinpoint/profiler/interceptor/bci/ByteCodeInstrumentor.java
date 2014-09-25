package com.nhn.pinpoint.profiler.interceptor.bci;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.util.Scope;

/**
 * @author emeroad
 */
public interface ByteCodeInstrumentor {


    void checkLibrary(ClassLoader classLoader, String javassistClassName);

    InstrumentClass getClass(String javassistClassName) throws InstrumentException;

    Scope getScope(String scopeName);

    Class<?> defineClass(ClassLoader classLoader, String defineClass, ProtectionDomain protectedDomain) throws InstrumentException;

    Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN) throws InstrumentException;

//    Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN, Object[] params) throws InstrumentException;

    Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN, Object[] params, Class[] paramClazz) throws InstrumentException;
}
