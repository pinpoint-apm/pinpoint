package com.nhn.pinpoint.bootstrap.plugin;

import java.security.ProtectionDomain;

public interface ClassEditor {
    public byte[] edit(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classFileBuffer);
}
