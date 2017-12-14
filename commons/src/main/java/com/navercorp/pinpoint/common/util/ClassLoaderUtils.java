/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;

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

    private static final ClassLoader SYSTEM_CLASS_LOADER;
    private static final ClassLoader EXT_CLASS_LOADER;
    private static final ClassLoader BOOT_CLASS_LOADER;

    static {
        SYSTEM_CLASS_LOADER = ClassLoader.getSystemClassLoader();
        if (SYSTEM_CLASS_LOADER != null) {
            EXT_CLASS_LOADER = SYSTEM_CLASS_LOADER.getParent();
        } else {
            EXT_CLASS_LOADER = null;
        }
        if (EXT_CLASS_LOADER != null) {
            BOOT_CLASS_LOADER = EXT_CLASS_LOADER.getParent();
        } else {
            BOOT_CLASS_LOADER = null;
        }
    }

    private ClassLoaderUtils() {
    }

//    TODO check @CallerSensitive, Reflection.getCallerClass()
//    private static ClassLoader getClassLoader(ClassLoader classLoader) {
//        if (classLoader == null) {
//            return ClassLoader.getSystemClassLoader();
//        }
//        return classLoader;
//    }

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
        } catch (Throwable ignore) {
            // skip
        }
        // Timing for security exceptions is different when the ClassLoader is received as an argument
        return defaultClassLoaderCallable.getClassLoader();
    }

    public interface ClassLoaderCallable {
        ClassLoader getClassLoader();
    }


    public static boolean isJvmClassLoader(ClassLoader classLoader) {
        if (BOOT_CLASS_LOADER == classLoader || SYSTEM_CLASS_LOADER == classLoader || EXT_CLASS_LOADER == classLoader) {
            return true;
        }
        return false;
    }

    public static String dumpStandardClassLoader() {
        final StringBuilder buffer = new StringBuilder();
        appendClassLoaderLog(buffer, "SYSTEM_CLASS_LOADER", SYSTEM_CLASS_LOADER);
        appendClassLoaderLog(buffer, "EXT_CLASS_LOADER", EXT_CLASS_LOADER);
        appendClassLoaderLog(buffer, "BOOT_CLASS_LOADER", BOOT_CLASS_LOADER);
        return buffer.toString();
    }

    private static void appendClassLoaderLog(StringBuilder buffer, String classLoaderName, ClassLoader classLoader) {
        buffer.append(classLoaderName);
        buffer.append(':');
        if (classLoader == null) {
            buffer.append("null");
        } else {
            buffer.append(classLoader.toString());
        }
        buffer.append(", ");
    }

}
