/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.JvmType;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.ShutdownHookRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * @author Taejin Koo
 */
public class ShutdownHookRegisterProvider implements Provider<ShutdownHookRegister> {

    // SharedSecrets
    //   - 6,7,8 sun.misc.SharedSecrets
    //   - 9,10,11 jdk.internal.misc.SharedSecrets

    // JavaLangAccess
    //   - 6,7,8 sun.misc.JavaLangAccess
    //   - 9,10,11 jdk.internal.misc.JavaLangAccess

    // method
    //  * oracle
    //   - 6 void registerShutdownHook(int , Runnable)
    //   - 7 ~ void registerShutdownHook(int var1, boolean , Runnable );
    //  * openjdk & ibmj9
    //   - 6 not exist
    //   - 7 ~ void registerShutdownHook(int var1, boolean , Runnable );

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Java6
    private static final String JDK6_SHUTDOWN_HOOK_REGISTER = "com.navercorp.pinpoint.profiler.shutdown.Java6ShutdownHookRegister";
    // Java7,8
    private static final String JDK7_SHUTDOWN_HOOK_REGISTER = "com.navercorp.pinpoint.profiler.shutdown.Java7ShutdownHookRegister";
    // Java9,10,11
    private static final String JDK9_SHUTDOWN_HOOK_REGISTER = "com.navercorp.pinpoint.profiler.shutdown.Java9ShutdownHookRegister";

    private static final RuntimeShutdownHookRegister RUNTIME_SHUTDOWN_HOOK_REGISTER = new RuntimeShutdownHookRegister();

    private final String vendorName;

    public ShutdownHookRegisterProvider(ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig");
        }
        vendorName = profilerConfig.getProfilerJvmVendorName();
    }

    @Override
    public ShutdownHookRegister get() {
        JvmType jvmType = JvmType.fromVendor(vendorName);
        if (jvmType == JvmType.UNKNOWN) {
            jvmType = JvmUtils.getType();
        }
        final String classToLoad = getShutdownHookRegiterClassName(jvmType);
        return createShutdownHookRegister(classToLoad);
    }

    private String getShutdownHookRegiterClassName(JvmType jvmType) {
        final JvmVersion jvmVersion = JvmUtils.getVersion();

        if (jvmVersion.onOrAfter(JvmVersion.JAVA_9)) {
            return JDK9_SHUTDOWN_HOOK_REGISTER;
        }

        if (jvmVersion.onOrAfter(JvmVersion.JAVA_7)) {
            return JDK7_SHUTDOWN_HOOK_REGISTER;
        }

        if (jvmVersion.onOrAfter(JvmVersion.JAVA_6)) {
            if (jvmType == JvmType.ORACLE) {
                return JDK6_SHUTDOWN_HOOK_REGISTER;
            }
        }

        return null;
    }

    private ShutdownHookRegister createShutdownHookRegister(String classToLoad) {
        if (classToLoad == null) {
            return RUNTIME_SHUTDOWN_HOOK_REGISTER;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<ShutdownHookRegister> shutdownHookRegisterClass = (Class<ShutdownHookRegister>) Class.forName(classToLoad);

            try {
                Constructor<ShutdownHookRegister> shutdownHookRegisterConstructorConstructor = shutdownHookRegisterClass.getConstructor();
                return shutdownHookRegisterConstructorConstructor.newInstance();
            } catch (NoSuchMethodException e) {
                logger.warn("Unknown ShutdownHookRegister : {}", classToLoad);
                return RUNTIME_SHUTDOWN_HOOK_REGISTER;
            }
        } catch (Exception e) {
            logger.warn("Error creating ShutdownHookRegister [" + classToLoad + "]", e);
        }
        return RUNTIME_SHUTDOWN_HOOK_REGISTER;
    }

    private static class RuntimeShutdownHookRegister implements ShutdownHookRegister {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        public void register(Thread thread) {
            Runtime.getRuntime().addShutdownHook(thread);
            logger.info("register() completed. (ShutdownHook registered in java.lang.Runtime.)");
        }

    }

}
