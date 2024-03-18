package com.navercorp.pinpoint.plugin.hbase;

import java.util.Objects;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    static Class<?> getClass(ClassLoader classLoader, String className) {
        Objects.requireNonNull(className, "className");
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
