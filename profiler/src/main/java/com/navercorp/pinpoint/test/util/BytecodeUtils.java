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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

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
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }

        final InputStream is = classLoader.getResourceAsStream(className.replace('.', '/') + ".class");
        if (is == null) {
            throw new RuntimeException("No such class file: " + className);
        }

        ReadableByteChannel channel = Channels.newChannel(is);
        ByteBuffer buffer;

        try {
            buffer = ByteBuffer.allocate(is.available());

            while (channel.read(buffer) >= 0) {
                if (buffer.remaining() == 0) {
                    buffer.flip();
                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                    newBuffer.put(buffer);
                    buffer = newBuffer;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            close(is);
        }

        return buffer.array();
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
}
