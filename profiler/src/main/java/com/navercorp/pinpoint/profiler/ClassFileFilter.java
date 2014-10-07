package com.nhn.pinpoint.profiler;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public interface ClassFileFilter {
	public static final boolean SKIP = true;
	public static final boolean CONTINUE = false;

	boolean doFilter(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer);
}
