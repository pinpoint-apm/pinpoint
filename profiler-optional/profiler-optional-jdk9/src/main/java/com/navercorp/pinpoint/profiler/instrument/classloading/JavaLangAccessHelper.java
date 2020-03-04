/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.classloading;

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * @author jaehong.kim
 */
public class JavaLangAccessHelper {
    private static final Logger logger = LoggerFactory.getLogger(JavaLangAccessHelper.class);
    // Java 9 version over and after
    private static final String JAVA9_SHARED_SECRETS_CLASS_NAME = "jdk.internal.misc.SharedSecrets";
    private static final String JAVA9_JAVA_LANG_ACCESS_CLASS_NAME = "jdk.internal.misc.JavaLangAccess";
    // Java 12 version over and after
    private static final String JAVA12_SHARED_SECRETS_CLASS_NAME = "jdk.internal.access.SharedSecrets";
    private static final String JAVA12_JAVA_LANG_ACCESS_CLASS_NAME = "jdk.internal.access.JavaLangAccess";

    // Static method
    private static final String GET_JAVA_LANG_ACCESS_METHOD_NAME = "getJavaLangAccess";
    // Public method
    private static final String REGISTER_SHUTDOWN_HOOK_METHOD_NAME = "registerShutdownHook";
    private static final String DEFINE_CLASS_METHOD_NAME = "defineClass";

    private JavaLangAccessHelper() {
    }

    public static Object getJavaLangAccessObject() {
        return JavaLangAccessObjectHolder.JAVA_LANG_ACCESS_OBJECT;
    }

    public static MethodHandle getRegisterShutdownHookMethodHandle() {
        return RegisterShutdownHookMethodHandleHolder.REGISTER_SHUTDOWN_HOOK_METHOD_HANDLE;
    }

    public static MethodHandle getDefineClassMethodHandle() {
        return DefineClassMethodHandleHolder.DEFINE_CLASS_METHOD_HANDLE;
    }

    private static Object initJavaLangAccessObject() {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final JvmVersion version = JvmUtils.getVersion();

        try {
            final Class<?> sharedSecretsClazz = findSharedSecretsClass(lookup, version);
            final Class<?> javaLangAccessClazz = findJavaLangAccessClass(lookup, version);
            Object result = invokeGetJavaLangAccessMethod(lookup, sharedSecretsClazz, javaLangAccessClazz);
            return result;
        } catch (Throwable t) {
            logger.warn("Failed to initialized JavaLangAccess Object", t);
        }

        return null;
    }

    private static MethodHandle initRegisterShutdownHookMethodHandle() {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final JvmVersion version = JvmUtils.getVersion();

        try {
            Class<?> javaLangAccessClazz = findJavaLangAccessClass(lookup, version);
            return findRegisterShutdownHookMethodHandle(lookup, javaLangAccessClazz);
        } catch (Throwable t) {
            logger.warn("Failed to initialized registerShutdownHook MethodHandle", t);
        }

        return null;
    }

    private static MethodHandle initDefineClassMethodHandle() {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final JvmVersion version = JvmUtils.getVersion();

        try {
            Class<?> javaLangAccessClazz = findJavaLangAccessClass(lookup, version);
            return findDefineClassMethodHandle(lookup, javaLangAccessClazz);
        } catch (Throwable t) {
            logger.warn("Failed to initialized defineClass MethodHandle", t);
        }

        return null;
    }

    private static Class<?> findSharedSecretsClass(MethodHandles.Lookup lookup, JvmVersion version) throws IllegalAccessException, ClassNotFoundException {
        if (version.onOrAfter(JvmVersion.JAVA_12)) {
            return lookup.findClass(JAVA12_SHARED_SECRETS_CLASS_NAME);
        } else {
            return lookup.findClass(JAVA9_SHARED_SECRETS_CLASS_NAME);
        }
    }

    private static Class<?> findJavaLangAccessClass(MethodHandles.Lookup lookup, JvmVersion version) throws IllegalAccessException, ClassNotFoundException {
        if (version.onOrAfter(JvmVersion.JAVA_12)) {
            return lookup.findClass(JAVA12_JAVA_LANG_ACCESS_CLASS_NAME);
        } else {
            return lookup.findClass(JAVA9_JAVA_LANG_ACCESS_CLASS_NAME);
        }
    }

    private static Object invokeGetJavaLangAccessMethod(MethodHandles.Lookup lookup, Class<?> refc, Class<?> rtype) throws Throwable {
        final MethodHandle methodHandle = lookup.findStatic(refc, GET_JAVA_LANG_ACCESS_METHOD_NAME, MethodType.methodType(rtype));
        return methodHandle.invoke();
    }

    private static MethodHandle findRegisterShutdownHookMethodHandle(MethodHandles.Lookup lookup, Class<?> refc) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findVirtual(refc, REGISTER_SHUTDOWN_HOOK_METHOD_NAME, MethodType.methodType(void.class, int.class, boolean.class, Runnable.class));
    }

    private static MethodHandle findDefineClassMethodHandle(MethodHandles.Lookup lookup, Class<?> refc) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findVirtual(refc, DEFINE_CLASS_METHOD_NAME, MethodType.methodType(Class.class, ClassLoader.class, String.class, byte[].class, java.security.ProtectionDomain.class, String.class));
    }

    private static class JavaLangAccessObjectHolder {
        static final Object JAVA_LANG_ACCESS_OBJECT = initJavaLangAccessObject();
    }

    private static class RegisterShutdownHookMethodHandleHolder {
        static final MethodHandle REGISTER_SHUTDOWN_HOOK_METHOD_HANDLE = initRegisterShutdownHookMethodHandle();
    }

    private static class DefineClassMethodHandleHolder {
        static final MethodHandle DEFINE_CLASS_METHOD_HANDLE = initDefineClassMethodHandle();
    }
}