/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * <em>Consider this class private.</em> Provides various methods to determine the caller class.
 * <h3>Background</h3>
 * <p>This method, available only in the Oracle/Sun/OpenJDK implementations of the Java
 * Virtual Machine, is a much more efficient mechanism for determining the {@link Class} of the caller of a particular
 * method. When it is not available, a {@link SecurityManager} is the second-best option. When this is also not
 * possible, the {@code StackTraceElement[]} returned by {@link Throwable#getStackTrace()} must be used, and its
 * {@code String} class name converted to a {@code Class} using the slow {@link Class#forName} (which can add an extra
 * microsecond or more for each invocation depending on the runtime ClassLoader hierarchy).
 * </p>
 * <p>
 * During Java 8 development, the {@code sun.reflect.Reflection.getCallerClass(int)} was removed from OpenJDK, and this
 * change was back-ported to Java 7 in version 1.7.0_25 which changed the behavior of the call and caused it to be off
 * by one stack frame. This turned out to be beneficial for the survival of this API as the change broke hundreds of
 * libraries and frameworks relying on the API which brought much more attention to the intended API removal.
 * </p>
 * <p>
 * After much community backlash, the JDK team agreed to restore {@code getCallerClass(int)} and keep its existing
 * behavior for the rest of Java 7. However, the method is deprecated in Java 8, and current Java 9 development has not
 * addressed this API. Therefore, the functionality of this class cannot be relied upon for all future versions of
 * Java. It does, however, work just fine in Sun JDK 1.6, OpenJDK 1.6, Oracle/OpenJDK 1.7, and Oracle/OpenJDK 1.8.
 * Other Java environments may fall back to using {@link Throwable#getStackTrace()} which is significantly slower due
 * to examination of every virtual frame of execution.
 * </p>
 */
public final class ReflectionUtil {
    // copy & patch log4j2 3.0.0-SNAPSHOT
    // https://github.com/apache/logging-log4j2/blob/master/log4j-api-java9/src/main/java/org/apache/logging/log4j/util/StackLocator.java
    // performance report
    // https://github.com/apache/logging-log4j2/commit/fe57a508a305f896edfc979333273b829dd07f6d
    private static final Logger LOGGER = StatusLogger.getLogger();

    private static final boolean SUN_REFLECTION_SUPPORTED = true;
    private static final PrivateSecurityManager SECURITY_MANAGER;

    static {
        PrivateSecurityManager psm;
        try {
            final SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new RuntimePermission("createSecurityManager"));
            }
            psm = new PrivateSecurityManager();
        } catch (final SecurityException ignored) {
            LOGGER.debug(
                    "Not allowed to create SecurityManager. Falling back to slowest ReflectionUtil implementation.");
            psm = null;
        }
        SECURITY_MANAGER = psm;
    }

    public static boolean supportsFastReflection() {
        return SUN_REFLECTION_SUPPORTED;
    }

    private static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private static final StackWalker stackWalker = StackWalker.getInstance();

    public static Class<?> getCallerClass(final String fqcn) {
        return getCallerClass(fqcn, "");
    }

    public static Class<?> getCallerClass(final String fqcn, final String pkg) {
        return walker.walk(s -> s.dropWhile(f -> !f.getClassName().equals(fqcn)).
                dropWhile(f -> f.getClassName().equals(fqcn)).dropWhile(f -> !f.getClassName().startsWith(pkg)).
                findFirst()).map(StackWalker.StackFrame::getDeclaringClass).orElse(null);
    }

    public static Class<?> getCallerClass(final Class<?> anchor) {
        return walker.walk(s -> s.dropWhile(f -> !f.getDeclaringClass().equals(anchor)).
                dropWhile(f -> f.getDeclaringClass().equals(anchor)).findFirst()).
                map(StackWalker.StackFrame::getDeclaringClass).orElse(null);
    }

    public static Class<?> getCallerClass(final int depth) {
        return walker.walk(s -> s.skip(depth).findFirst()).map(StackWalker.StackFrame::getDeclaringClass).orElse(null);
    }

    public static Stack<Class<?>> getCurrentStackTrace() {
        // benchmarks show that using the SecurityManager is much faster than looping through getCallerClass(int)
        if (SECURITY_MANAGER != null) {
            return getCurrentStackTracegetPrivateSecurity();
        }
        Stack<Class<?>> stack = new Stack<Class<?>>();
        List<Class<?>> classes = walker.walk(s -> s.map(f -> f.getDeclaringClass()).collect(Collectors.toList()));
        stack.addAll(classes);
        return stack;
    }

    public static StackTraceElement calcLocation(final String fqcnOfLogger) {
        return stackWalker.walk(
                s -> s.dropWhile(f -> !f.getClassName().equals(fqcnOfLogger)) // drop the top frames until we reach the logger
                        .dropWhile(f -> f.getClassName().equals(fqcnOfLogger)) // drop the logger frames
                        .findFirst()).map(StackWalker.StackFrame::toStackTraceElement).orElse(null);
    }

    public static StackTraceElement getStackTraceElement(final int depth) {
        return stackWalker.walk(s -> s.skip(depth).findFirst())
                .map(StackWalker.StackFrame::toStackTraceElement).orElse(null);
    }

    // benchmarks show that using the SecurityManager is much faster than looping through getCallerClass(int)
    static Stack<Class<?>> getCurrentStackTracegetPrivateSecurity() {
        final Class<?>[] array = SECURITY_MANAGER.getClassContext();
        final Stack<Class<?>> classes = new Stack<>();
        classes.ensureCapacity(array.length);
        for (final Class<?> clazz : array) {
            classes.push(clazz);
        }
        return classes;
    }

    static final class PrivateSecurityManager extends SecurityManager {

        @Override
        protected Class<?>[] getClassContext() {
            return super.getClassContext();
        }

    }

    private ReflectionUtil() {
    }
}
