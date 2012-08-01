package com.profiler.modifier;

public interface Modifier {
	byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer);
	String getTargetClass();
}
