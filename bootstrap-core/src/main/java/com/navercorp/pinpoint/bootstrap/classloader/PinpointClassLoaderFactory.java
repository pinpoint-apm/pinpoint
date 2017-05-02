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

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Taejin Koo
 */
public final class PinpointClassLoaderFactory {

    private static final PLogger LOGGER = PLoggerFactory.getLogger(PinpointClassLoaderFactory.class);

    private static final InnerPinpointClassLoaderFactory CLASS_LOADER_FACTORY = createClassLoaderFactory();

    // Jdk 7+
    private static final String PARALLEL_CAPABLE_CLASS_LOADER_FACTORY = "com.navercorp.pinpoint.bootstrap.classloader.ParallelCapablePinpointClassLoaderFactory";

    private PinpointClassLoaderFactory() {
        throw new IllegalAccessError();
    }

    private static InnerPinpointClassLoaderFactory createClassLoaderFactory() {
        final JvmVersion jvmVersion = JvmUtils.getVersion();
        if (jvmVersion == JvmVersion.JAVA_6) {
            return new DefaultPinpointClassLoaderFactory();
        } else if (jvmVersion.onOrAfter(JvmVersion.JAVA_7)) {
            boolean hasRegisterAsParallelCapableMethod = hasRegisterAsParallelCapableMethod();
            if (hasRegisterAsParallelCapableMethod) {
                try {
                    ClassLoader classLoader = getClassLoader(PinpointClassLoaderFactory.class.getClassLoader());
                    final Class<? extends InnerPinpointClassLoaderFactory> parallelCapableClassLoaderFactoryClass =
                            (Class<? extends InnerPinpointClassLoaderFactory>) Class.forName(PARALLEL_CAPABLE_CLASS_LOADER_FACTORY, true, classLoader);
                    return parallelCapableClassLoaderFactoryClass.newInstance();
                } catch (ClassNotFoundException e) {
                    logError(e);
                } catch (InstantiationException e) {
                    logError(e);
                } catch (IllegalAccessException e) {
                    logError(e);
                }
                return new DefaultPinpointClassLoaderFactory();
            } else {
                return new DefaultPinpointClassLoaderFactory();
            }
        } else {
            throw new RuntimeException("Unsupported jvm version " + jvmVersion);
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
        if (classLoader == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }

    private static void logError(Exception e) {
        LOGGER.info("ParallelCapablePinpointClassLoader not found.");
    }

    public static URLClassLoader createClassLoader(URL[] urls, ClassLoader parent) {
        return CLASS_LOADER_FACTORY.createURLClassLoader(urls, parent);
    }

    public static URLClassLoader createClassLoader(URL[] urls, ClassLoader parent, LibClass libClass) {
        return CLASS_LOADER_FACTORY.createURLClassLoader(urls, parent, libClass);
    }

}
