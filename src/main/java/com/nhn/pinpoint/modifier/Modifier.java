package com.nhn.pinpoint.modifier;

import java.security.ProtectionDomain;

public interface Modifier {
	byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer);

	String getTargetClass();
}
