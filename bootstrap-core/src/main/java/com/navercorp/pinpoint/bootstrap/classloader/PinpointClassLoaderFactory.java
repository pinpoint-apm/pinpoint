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
import java.net.URL;
import java.util.List;

/**
 * @author Taejin Koo
 */
public final class PinpointClassLoaderFactory {

    private static final PLogger LOGGER = PLoggerFactory.getLogger(PinpointClassLoaderFactory.class);

    private static final ClassLoaderFactory CLASS_LOADER_FACTORY = createClassLoaderFactory();

    // Jdk 7+
    private static final String PARALLEL_CLASS_LOADER_FACTORY = "com.navercorp.pinpoint.bootstrap.classloader.ParallelClassLoaderFactory";

    // jdk9
    private static final String JAVA9_CLASSLOADER = "com.navercorp.pinpoint.bootstrap.java9.classloader.Java9ClassLoader";


    private PinpointClassLoaderFactory() {
        throw new IllegalAccessError();
    }

    public static ClassLoaderFactory createClassLoaderFactory() {
        final JvmVersion jvmVersion = JvmUtils.getVersion();

        if (jvmVersion.onOrAfter(JvmVersion.JAVA_9)) {
            return newClassLoaderFactory(JAVA9_CLASSLOADER);
        }

        // URLClassLoader not work for java9
        if (disableChildFirst()) {
            return new URLClassLoaderFactory();
        }

        if (jvmVersion.onOrAfter(JvmVersion.JAVA_7)) {
            return newParallelClassLoaderFactory();
        }

        // JDK6 --
        return new Java6ClassLoaderFactory();
    }

    private static boolean disableChildFirst() {
        String disable = System.getProperty("pinpoint.agent.classloader.childfirst.disable");
        return "true".equalsIgnoreCase(disable);
    }

    private static ClassLoaderFactory newClassLoaderFactory(String factoryName) {
        ClassLoader classLoader = PinpointClassLoaderFactory.class.getClassLoader();

        return new DynamicClassLoaderFactory(factoryName, classLoader);
    }

    private static ClassLoaderFactory newParallelClassLoaderFactory() {
        try {
            ClassLoader classLoader = PinpointClassLoaderFactory.class.getClassLoader();
            final Class<? extends ClassLoaderFactory> classLoaderFactoryClazz =
                    (Class<? extends ClassLoaderFactory>) Class.forName(PARALLEL_CLASS_LOADER_FACTORY, true, classLoader);
            Constructor<? extends ClassLoaderFactory> constructor = classLoaderFactoryClazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(PARALLEL_CLASS_LOADER_FACTORY  + " create fail Caused by:" + e.getMessage(), e);
        }
    }


    public static ClassLoader createClassLoader(String name, URL[] urls, ClassLoader parent, List<String> libClass) {
        return CLASS_LOADER_FACTORY.createClassLoader(name, urls, parent, libClass);
    }

}
