/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.classloader;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author Taejin Koo
 */
public final class PinpointClassLoaderFactory {

    private static final PLogger LOGGER = PLoggerFactory.getLogger(PinpointClassLoaderFactory.class);

    private static final InnerClassLoaderFactory CLASS_LOADER_FACTORY = createClassLoaderFactory();

    // Jdk 7+
    private static final String PARALLEL_CLASSLOADER_FACTORY = "com.navercorp.pinpoint.bootstrap.classloader.ParallelClassLoaderFactory";

    // jdk9
    private static final String JAVA9_CLASSLOADER_FACTORY = "com.navercorp.pinpoint.bootstrap.classloader.Java9ClassLoaderFactory";


    private PinpointClassLoaderFactory() {
        throw new IllegalAccessError();
    }

    private static InnerClassLoaderFactory createClassLoaderFactory() {
        final JvmVersion jvmVersion = JvmUtils.getVersion();

        if (jvmVersion.onOrAfter(JvmVersion.JAVA_9)) {
            if (!hasRegisterAsParallelCapableMethod()) {
                throw new IllegalStateException("ClassLoader.registerAsParallelCapable() not supported");
            }
            return newClassLoaderFactory(JAVA9_CLASSLOADER_FACTORY);
        }
        if (jvmVersion.onOrAfter(JvmVersion.JAVA_7)) {
            if (!hasRegisterAsParallelCapableMethod()) {
                return new Java6ClassLoaderFactory();
            }
            return newClassLoaderFactory(PARALLEL_CLASSLOADER_FACTORY);
        }

        // JDK6 --
        return new Java6ClassLoaderFactory();
    }

    private static InnerClassLoaderFactory newClassLoaderFactory(String factoryName) {
        try {
            ClassLoader classLoader = getClassLoader(PinpointClassLoaderFactory.class.getClassLoader());
            final Class<? extends InnerClassLoaderFactory> parallelCapableClassLoaderFactoryClass =
                    (Class<? extends InnerClassLoaderFactory>) Class.forName(factoryName, true, classLoader);
            Constructor<? extends InnerClassLoaderFactory> constructor = parallelCapableClassLoaderFactoryClass.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception ex) {
            LOGGER.info("ParallelCapablePinpointClassLoader not found. {}", ex.getMessage(), ex);
            throw new IllegalStateException("ParallelCapablePinpointClassLoader initialize fail Caused by:" + ex.getMessage(), ex);
        }
    }

    private static boolean hasRegisterAsParallelCapableMethod() {
        Method[] methods = ClassLoader.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("registerAsParallelCapable")) {
                return true;
            }
        }

        return false;
    }

    private static ClassLoader getClassLoader(ClassLoader classLoader) {
//         @Nullable  (class == null) is Bootstrap
//        if (classLoader == null) {
//            return ClassLoader.getSystemClassLoader();
//        }
        return classLoader;
    }

    public static ClassLoader createClassLoader(URL[] urls, ClassLoader parent) {
        return CLASS_LOADER_FACTORY.createURLClassLoader(urls, parent);
    }

    public static ClassLoader createClassLoader(URL[] urls, ClassLoader parent, LibClass libClass) {
        return CLASS_LOADER_FACTORY.createURLClassLoader(urls, parent, libClass);
    }

}
