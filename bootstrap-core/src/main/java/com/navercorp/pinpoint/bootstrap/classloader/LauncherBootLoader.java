/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.classloader;

import sun.misc.Launcher;
import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author Woonduk Kang(emeroad)
 */
final class LauncherBootLoader implements BootLoader {

    private final URLClassPath bootstrapClassPath = getBootstrapClassPath();

    private static final Method FIND_BOOTSTRAP_CLASS_OR_NULL = findBootstrapClassOrNullMethod();

    private static Method findBootstrapClassOrNullMethod() {
        // findBootstrapClassOrNull() jdk Compatibility
        // oracleJdk
        // oracleJdk 9~10 : reflection is not recommended

        // old OracleJdk :
        // oracleJdk6_24 : success

        // openjdk
        // openjdk8 : success
        // openjdk7 : success
        // openjdk6 : fail (findBootstrapClass0)
        NoSuchMethodException rootException;
        try {
            Method findBootstrapClassOrNull = ClassLoader.class.getDeclaredMethod("findBootstrapClassOrNull", String.class);
            findBootstrapClassOrNull.setAccessible(true);
            return findBootstrapClassOrNull;
        } catch (NoSuchMethodException ex) {
            rootException = ex;
        }
        try {
            // for openjdk6
            Method findBootstrapClass0 = ClassLoader.class.getDeclaredMethod("findBootstrapClass0", String.class);
            findBootstrapClass0.setAccessible(true);
            return findBootstrapClass0;
        } catch (NoSuchMethodException ignore) {
            // skip
        }
        throw new IllegalStateException("ClassLoader.findBootstrapClassOrNull api not found", rootException);
    }

    static {
        // preload
        new URLEnumeration(null);
    }

    LauncherBootLoader() {
    }

    private static URLClassPath getBootstrapClassPath() {
        return Launcher.getBootstrapClassPath();
    }

    @Override
    public URL findResource(String name) {
        final Resource res = bootstrapClassPath.getResource(name);
        if (res == null) {
            return null;
        }
        return res.getURL();
    }

    @Override
    public Class<?> findBootstrapClassOrNull(ClassLoader classLoader, String name) {
        try {
            return (Class<?>) FIND_BOOTSTRAP_CLASS_OR_NULL.invoke(classLoader, name);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(FIND_BOOTSTRAP_CLASS_OR_NULL.getName() + "() access fail " + ex.getMessage(), ex);
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof ClassNotFoundException) {
                // fix openjdk6
                return null;
            }
            throw new IllegalStateException(FIND_BOOTSTRAP_CLASS_OR_NULL.getName() + "() internal error " + ex.getMessage(), ex);
        }
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        final Enumeration<Resource> enumeration = bootstrapClassPath.getResources(name);
        return new URLEnumeration(enumeration);
    }

    private static class URLEnumeration implements Enumeration<URL> {
        private final Enumeration<Resource> enumeration;

        private URLEnumeration(Enumeration<Resource> enumeration) {
            this.enumeration = enumeration;
        }

        public URL nextElement() {
            Resource resource = enumeration.nextElement();
            return resource.getURL();
        }

        public boolean hasMoreElements() {
            return enumeration.hasMoreElements();
        }
    }
}
