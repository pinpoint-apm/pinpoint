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

    private ClassLoaderUtils() {
    }

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
}
