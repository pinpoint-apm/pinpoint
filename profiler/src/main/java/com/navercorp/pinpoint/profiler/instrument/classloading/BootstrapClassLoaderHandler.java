/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class BootstrapClassLoaderHandler implements ClassInjector {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PluginConfig pluginConfig;

    private final Object lock = new Object();
    private boolean injectedToRoot = false;

    private final InstrumentEngine instrumentEngine;


    public BootstrapClassLoaderHandler(PluginConfig pluginConfig, InstrumentEngine instrumentEngine) {
        if (pluginConfig == null) {
            throw new NullPointerException("pluginConfig must not be null");
        }
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        this.pluginConfig = pluginConfig;
        this.instrumentEngine = instrumentEngine;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        try {
            if (classLoader == null) {
                return (Class<T>) injectClass0(className);
            }
        } catch (Exception e) {
            logger.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new PinpointException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
        throw new PinpointException("invalid ClassLoader");
    }

    private Class<?> injectClass0(String className) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        appendToBootstrapClassLoaderSearch();
        return Class.forName(className, false, null);
    }

    private void appendToBootstrapClassLoaderSearch() {
        synchronized (lock) {
            if (this.injectedToRoot == false) {
                this.injectedToRoot = true;
                instrumentEngine.appendToBootstrapClassPath(pluginConfig.getPluginJarFile());
            }
        }
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String classPath) {
        try {
            if (targetClassLoader == null) {
                ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                if (classLoader == null) {
                    return null;
                }
                appendToBootstrapClassLoaderSearch();
                return classLoader.getResourceAsStream(classPath);
            }
        } catch (Exception e) {
            logger.warn("Failed to load plugin resource as stream {} with classLoader {}", classPath, targetClassLoader, e);
            return null;
        }
        logger.warn("Invalid bootstrap class loader. cl={}", targetClassLoader);
        return null;
    }
}