package com.navercorp.pinpoint.test.plugin.util;

public final class ClassLoaderUtils {
    private ClassLoaderUtils() {
    }

    public static ClassLoader getContextClassLoader() {
        final Thread thread = Thread.currentThread();
        return thread.getContextClassLoader();
    }

}
