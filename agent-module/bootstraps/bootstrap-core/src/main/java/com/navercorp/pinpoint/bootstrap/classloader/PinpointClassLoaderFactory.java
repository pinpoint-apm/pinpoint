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

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public final class PinpointClassLoaderFactory {

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


        return newParallelClassLoaderFactory();
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
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(PARALLEL_CLASS_LOADER_FACTORY  + " create fail Caused by:" + e.getMessage(), e);
        }
    }

    public static ClassLoader createClassLoader(String name, URL[] urls, ClassLoader parent, List<String> libClass) {
        return CLASS_LOADER_FACTORY.createClassLoader(name, urls, parent, libClass);
    }


    public static ClassLoader createClassLoader(String name, URL[] urls, ClassLoader parent, Properties properties) {
        List<String> libClass = getAgentClassloaderLibs(properties);
        return createClassLoader(name, urls, parent, libClass);
    }

    public static final String AGENT_CLASSLOADER_ADDITIONAL_LIBS = "profiler.agent.classloader.additional-libs";

    private static List<String> getAgentClassloaderLibs(Properties properties) {
        Set<String> libs = new HashSet<>(ProfilerLibs.PINPOINT_PROFILER_CLASS);

        String libsString = properties.getProperty(AGENT_CLASSLOADER_ADDITIONAL_LIBS, "");
        List<String> additionalLibs = StringUtils.tokenizeToStringList(libsString, ",");
        libs.addAll(additionalLibs);

        List<String> copy = new ArrayList<>(libs);
        copy.sort(String.CASE_INSENSITIVE_ORDER);
        return copy;
    }
}
