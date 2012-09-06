package com.profiler.interceptor.bci;

import java.security.ProtectionDomain;

import javassist.ClassPool;

public interface ByteCodeInstrumentor {

	// 임시로 만들자.
	ClassPool getClassPool();

	void checkLibrary(ClassLoader classLoader, String javassistClassName);

	InstrumentClass getClass(String javassistClassName) throws InstrumentException;

	Class<?> defineClass(ClassLoader classLoader, String defineClass, ProtectionDomain protectedDomain) throws InstrumentException;
}
