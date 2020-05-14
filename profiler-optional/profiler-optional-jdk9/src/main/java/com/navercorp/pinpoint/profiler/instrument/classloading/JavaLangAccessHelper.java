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
import com.navercorp.pinpoint.common.util.SystemPropertyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.ProtectionDomain;

/**
 * @author jaehong.kim
 */
public final class JavaLangAccessHelper {
    private static final Logger logger = LoggerFactory.getLogger(JavaLangAccessHelper.class);
    // Java 9 version over and after
    private static final String MISC_SHARED_SECRETS_CLASS_NAME = "jdk.internal.misc.SharedSecrets";
    private static final String MISC_JAVA_LANG_ACCESS_CLASS_NAME = "jdk.internal.misc.JavaLangAccess";
    // Java 12 version over and after
    private static final String ACCESS_SHARED_SECRETS_CLASS_NAME = "jdk.internal.access.SharedSecrets";
    private static final String ACCESS_JAVA_LANG_ACCESS_CLASS_NAME = "jdk.internal.access.JavaLangAccess";

    // Static method
    private static final String GET_JAVA_LANG_ACCESS_METHOD_NAME = "getJavaLangAccess";
    // Public method
    private static final String REGISTER_SHUTDOWN_HOOK_METHOD_NAME = "registerShutdownHook";
    private static final String DEFINE_CLASS_METHOD_NAME = "defineClass";

    private static final Object JAVA_LANG_ACCESS_OBJECT = initJavaLangAccessObject();
    private static final MethodHandle DEFINE_CLASS_METHOD_HANDLE = initDefineClassMethodHandle();
    private static final MethodHandle REGISTER_SHUTDOWN_HOOK_METHOD_HANDLE = initRegisterShutdownHookMethodHandle();


    private JavaLangAccessHelper() {
    }

    public static Object getJavaLangAccessObject() {
        return JAVA_LANG_ACCESS_OBJECT;
    }

    // @Nullable
    public static MethodHandle getRegisterShutdownHookMethodHandle() {
        return REGISTER_SHUTDOWN_HOOK_METHOD_HANDLE;
    }

    public static MethodHandle getDefineClassMethodHandle() {
        return DEFINE_CLASS_METHOD_HANDLE;
    }

    private static Object initJavaLangAccessObject() {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        try {
            final Class<?> sharedSecretsClazz = findSharedSecretsClass(lookup);
            final Class<?> javaLangAccessClazz = findJavaLangAccessClass(lookup);
            Object result = invokeGetJavaLangAccessMethod(lookup, sharedSecretsClazz, javaLangAccessClazz);
            return result;
        } catch (Throwable t) {
            logger.error("Failed to initialized JavaLangAccess Object", t);
            dumpJdkInfo();
            // ERROR jdk compatibility issue
            throw new IncompatibleClassChangeError("JavaLangAccess lookup fail");
        }
    }

    private static MethodHandle initDefineClassMethodHandle() {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        try {
            Class<?> javaLangAccessClazz = findJavaLangAccessClass(lookup);
            return findDefineClassMethodHandle(lookup, javaLangAccessClazz);
        } catch (Throwable t) {
            logger.error("Failed to initialized defineClass MethodHandle", t);
            dumpJdkInfo();
            throw new IncompatibleClassChangeError("defineClass MethodHandle lookup fail");
        }
    }

    // for debugging
    private static void dumpJdkInfo() {
        logger.warn("Dump JDK info java.vm.name:{} java.version:{}", JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VM_NAME), JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VM_VERSION));
    }

    private static MethodHandle initRegisterShutdownHookMethodHandle() {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        try {
            final Class<?> javaLangAccessClazz = findJavaLangAccessClass(lookup);
            return findRegisterShutdownHookMethodHandle(lookup, javaLangAccessClazz);
        } catch (Throwable t) {
            logger.warn("Failed to initialized registerShutdownHook MethodHandle", t);
            dumpJdkInfo();
        }
        return null;
    }



    private static Class<?> findSharedSecretsClass(MethodHandles.Lookup lookup) throws IllegalAccessException, ClassNotFoundException {
        try {
            // https://github.com/naver/pinpoint/issues/6752
            // Oracle JDK11 : jdk.internal.access
            // openJDK11 =  jdk.internal.misc
            return lookup.findClass(ACCESS_SHARED_SECRETS_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            return lookup.findClass(MISC_SHARED_SECRETS_CLASS_NAME);
        }
    }

    private static Class<?> findJavaLangAccessClass(MethodHandles.Lookup lookup) throws IllegalAccessException, ClassNotFoundException {
        try {
            // https://github.com/naver/pinpoint/issues/6752
            // Oracle JDK11 : jdk.internal.access
            // openJDK11 =  jdk.internal.misc
            return lookup.findClass(ACCESS_JAVA_LANG_ACCESS_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            return lookup.findClass(MISC_JAVA_LANG_ACCESS_CLASS_NAME);
        }
    }


    private static Object invokeGetJavaLangAccessMethod(MethodHandles.Lookup lookup, Class<?> refc, Class<?> rtype) throws Throwable {
        MethodType methodType = MethodType.methodType(rtype);
        final MethodHandle methodHandle = lookup.findStatic(refc, GET_JAVA_LANG_ACCESS_METHOD_NAME, methodType);
        return methodHandle.invoke();
    }

    private static MethodHandle findRegisterShutdownHookMethodHandle(MethodHandles.Lookup lookup, Class<?> refc) throws NoSuchMethodException, IllegalAccessException {
        MethodType methodType = MethodType.methodType(void.class, int.class, boolean.class, Runnable.class);
        return lookup.findVirtual(refc, REGISTER_SHUTDOWN_HOOK_METHOD_NAME, methodType);
    }

    private static MethodHandle findDefineClassMethodHandle(MethodHandles.Lookup lookup, Class<?> refc) throws NoSuchMethodException, IllegalAccessException {
        MethodType methodType = MethodType.methodType(Class.class, ClassLoader.class, String.class, byte[].class, ProtectionDomain.class, String.class);
        return lookup.findVirtual(refc, DEFINE_CLASS_METHOD_NAME, methodType);
    }

}