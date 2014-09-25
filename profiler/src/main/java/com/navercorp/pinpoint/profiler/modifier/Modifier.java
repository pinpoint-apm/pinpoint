package com.nhn.pinpoint.profiler.modifier;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public interface Modifier {
	byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer);

	String getTargetClass();
}
