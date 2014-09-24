package com.nhn.pinpoint.common.util;

/**
 * @author emeroad
 */
public final class ClassLoaderUtils {

    public static ClassLoader getClassLoader() {
        return getDefaultClassLoader(ClassLoaderUtils.class.getClassLoader());
    }

    public static ClassLoader getDefaultClassLoader(ClassLoader defaultClassLoader) {
        ClassLoader classLoader = null;
        try {
            final Thread th = Thread.currentThread();
            classLoader = th.getContextClassLoader();
        } catch (Throwable e) {
            // skip
        }
        if (classLoader == null) {
            return defaultClassLoader;
        }
        return classLoader;
    }

}
