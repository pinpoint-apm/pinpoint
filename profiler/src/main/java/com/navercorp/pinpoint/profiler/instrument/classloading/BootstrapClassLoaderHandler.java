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

import java.util.Objects;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.InputStream;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class BootstrapClassLoaderHandler implements ClassInjector {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PluginConfig pluginConfig;
    private final InstrumentEngine instrumentEngine;

    private final Object lock = new Object();
    private volatile boolean injectedToRoot = false;

    public BootstrapClassLoaderHandler(PluginConfig pluginConfig, InstrumentEngine instrumentEngine) {
        this.pluginConfig = Objects.requireNonNull(pluginConfig, "pluginConfig");
        this.instrumentEngine = Objects.requireNonNull(instrumentEngine, "instrumentEngine");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        if (classLoader != Object.class.getClassLoader()) {
            throw new IllegalStateException("not BootStrapClassLoader");
        }
        try {
            return (Class<T>) injectClass0(className);
        } catch (Exception e) {
            logger.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new PinpointException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
    }

    private Class<?> injectClass0(String className) throws IllegalArgumentException, ClassNotFoundException {
        appendToBootstrapClassLoaderSearch();
        return Class.forName(className, false, null);
    }

    private void appendToBootstrapClassLoaderSearch() {
        // DCL
        if (injectedToRoot) {
            return;
        }
        synchronized (lock) {
            if (this.injectedToRoot == false) {
                instrumentEngine.appendToBootstrapClassPath(pluginConfig.getPluginJarFile());
                // Memory visibility WARNING
                // Reordering is not recommended.
                this.injectedToRoot = true;
            }
        }
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String internalName) {
        try {
            if (targetClassLoader == null) {
                ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                if (classLoader == null) {
                    return null;
                }
                appendToBootstrapClassLoaderSearch();
                return classLoader.getResourceAsStream(internalName);
            }
        } catch (Exception e) {
            logger.warn("Failed to load plugin resource as stream {} with classLoader {}", internalName, targetClassLoader, e);
            return null;
        }
        logger.warn("Invalid bootstrap class loader. cl={}", targetClassLoader);
        return null;
    }
}