package com.profiler.modifier;

public interface Modifier {
	byte[] modify(ClassLoader classLoader, String className, byte[] classFileBuffer);
	String getTargetClass();
}
