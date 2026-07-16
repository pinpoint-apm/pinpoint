/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.classloading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

/**
 * JDK 8 only. sun.misc.Unsafe.defineClass supports a null(bootstrap) classloader,
 * unlike ClassLoader.defineClass. The method was removed in JDK 11, so it is
 * resolved reflectively and must not be reached on JDK 9+.
 */
final class UnsafeDefineClass implements DefineClass {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final Object UNSAFE;
    private static final Method DEFINE_CLASS;

    static {
        try {
            final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            final Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = theUnsafe.get(null);
            DEFINE_CLASS = unsafeClass.getMethod("defineClass",
                    String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot access sun.misc.Unsafe.defineClass", e);
        }
    }

    @Override
    public Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
        if (logger.isDebugEnabled()) {
            logger.debug("define class:{} cl:{}", name, classLoader);
        }
        try {
            return (Class<?>) DEFINE_CLASS.invoke(UNSAFE, name, bytes, 0, bytes.length, classLoader, null);
        } catch (InvocationTargetException e) {
            // unwrap: the message of the LinkageError/ClassFormatError thrown by the VM is on the cause
            final Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw handleDefineClassFail(classLoader, name, cause);
        } catch (ReflectiveOperationException e) {
            throw handleDefineClassFail(classLoader, name, e);
        }
    }

    private RuntimeException handleDefineClassFail(ClassLoader classLoader, String className, Throwable cause) {
        logger.warn("{} define fail cl:{} Caused by:{}", className, classLoader, cause.getMessage(), cause);
        return new RuntimeException(className + " define fail Caused by:" + cause.getMessage(), cause);
    }
}
