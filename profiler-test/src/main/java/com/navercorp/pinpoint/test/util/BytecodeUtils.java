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

package com.navercorp.pinpoint.test.util;

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

// TODO move package
public final class BytecodeUtils {

    private static final Method DEFINE_CLASS = getDefineClassMethod();

    private BytecodeUtils() {
    }


    private static Method getDefineClassMethod() {
        try {
            final Method method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            // link error
            throw new RuntimeException("defineClass not found. Error:" + e.getMessage(), e);
        } catch (SecurityException e) {
            // link error
            throw new RuntimeException("defineClass error. Error:" + e.getMessage(), e);
        }
    }

    public static Class<?> defineClass(ClassLoader classLoader, String className, byte[] classFile) {
        try {
            return (Class<?>) DEFINE_CLASS.invoke(classLoader, className, classFile, 0, classFile.length);
        } catch (Exception ex) {
            throw new RuntimeException("defineClass error. Caused:" + ex.getMessage(), ex);
        }
    }

    public static byte[] getClassFile(ClassLoader classLoader, String className) {
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }
        classLoader = getClassLoader(classLoader);

        final String classInternalName = JavaAssistUtils.javaNameToJvmName(className);
        final InputStream is = classLoader.getResourceAsStream(classInternalName + ".class");
        if (is == null) {
            throw new RuntimeException("No such class file: " + className);
        }

        try {
            return readClass(is, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            close(is);
        }
    }

    /**
     * COPY ASM method. reference : org.objectweb.asm.ClassReader
     *
     * Reads the bytecode of a class.
     *
     * @param is
     *            an input stream from which to read the class.
     * @param close
     *            true to close the input stream after reading.
     * @return the bytecode read from the given input stream.
     * @throws IOException
     *             if a problem occurs during reading.
     */
    public static byte[] readClass(final InputStream is, boolean close)
            throws IOException {
        if (is == null) {
            throw new IOException("Class not found");
        }
        try {
            byte[] b = new byte[is.available()];
            int len = 0;
            while (true) {
                int n = is.read(b, len, b.length - len);
                if (n == -1) {
                    if (len < b.length) {
                        byte[] c = new byte[len];
                        System.arraycopy(b, 0, c, 0, len);
                        b = c;
                    }
                    return b;
                }
                len += n;
                if (len == b.length) {
                    int last = is.read();
                    if (last < 0) {
                        return b;
                    }
                    byte[] c = new byte[b.length + 1000];
                    System.arraycopy(b, 0, c, 0, len);
                    c[len++] = (byte) last;
                    b = c;
                }
            }
        } finally {
            if (close) {
                close(is);
            }
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
                // skip
            }
        }
    }

    private static ClassLoader getClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }
}
