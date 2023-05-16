package jdk.internal.access;

import java.security.ProtectionDomain;

public class SharedSecrets {
    public static JavaLangAccess getJavaLangAccess() {
        return new JavaLangAccess() {
            @Override
            public Class<?> defineClass(ClassLoader cl, String name, byte[] b, ProtectionDomain pd, String source) {
                return null;
            }

            @Override
            public Class<?> defineClass(ClassLoader cl, Class<?> lookup, String name, byte[] b, ProtectionDomain pd, boolean initialize, int flags, Object classData) {
                return null;
            }

            @Override
            public void registerShutdownHook(int slot, boolean registerShutdownInProgress, Runnable hook) {

            }
        };
    }
}
