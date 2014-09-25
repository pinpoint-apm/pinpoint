package com.nhn.pinpoint.common.util;

/**
 * @author emeroad
 */
public final class ClassLoaderUtils {

    public static final ClassLoaderCallable DEFAULT_CLASS_LOADER_CALLABLE = new ClassLoaderCallable() {
        @Override
        public ClassLoader getClassLoader() {
            return ClassLoaderUtils.class.getClassLoader();
        }
    };

    public static ClassLoader getDefaultClassLoader() {
        return getDefaultClassLoader(DEFAULT_CLASS_LOADER_CALLABLE);
    }

    public static ClassLoader getDefaultClassLoader(ClassLoaderCallable defaultClassLoaderCallable) {
        if (defaultClassLoaderCallable == null) {
            throw new NullPointerException("defaultClassLoaderCallable must not be null");
        }

        try {
            final Thread th = Thread.currentThread();
            final ClassLoader contextClassLoader = th.getContextClassLoader();
            if (contextClassLoader != null) {
                return contextClassLoader;
            }
        } catch (Throwable e) {
            // skip
        }
        // 파라미터로 ClassLoader를 전달 받으면 security exception 의 발생타이밍이 다르다.
        return defaultClassLoaderCallable.getClassLoader();
    }

    public static interface ClassLoaderCallable {
        ClassLoader getClassLoader();
    }
}
