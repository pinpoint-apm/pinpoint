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

import java.io.InputStream;
import java.net.URLClassLoader;

import java.util.Objects;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class JarProfilerPluginClassInjector implements ClassInjector {
    private final Logger logger = LoggerFactory.getLogger(JarProfilerPluginClassInjector.class);

    private final BootstrapCore bootstrapCore;
    private final ClassInjector bootstrapClassLoaderHandler;
    private final ClassInjector urlClassLoaderHandler;
    private final ClassInjector plainClassLoaderHandler;

    public JarProfilerPluginClassInjector(PluginConfig pluginConfig, InstrumentEngine instrumentEngine, BootstrapCore bootstrapCore) {
        if (pluginConfig == null) {
            throw new NullPointerException("pluginConfig");
        }
        this.bootstrapCore = Objects.requireNonNull(bootstrapCore, "bootstrapCore");
        this.bootstrapClassLoaderHandler = new BootstrapClassLoaderHandler(pluginConfig, instrumentEngine);
        this.urlClassLoaderHandler = new URLClassLoaderHandler(pluginConfig);
        this.plainClassLoaderHandler = new PlainClassLoaderHandler(pluginConfig);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        try {
            if (bootstrapCore.isBootstrapPackage(className)) {
                return bootstrapCore.loadClass(className);
            }
            if (classLoader == null) {
                return (Class<T>)bootstrapClassLoaderHandler.injectClass(null, className);
            } else if (classLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                return (Class<T>)urlClassLoaderHandler.injectClass(urlClassLoader, className);
            } else {
                return (Class<T>)plainClassLoaderHandler.injectClass(classLoader, className);
            }
        } catch (Throwable e) {
            // fixed for LinkageError
            logger.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new PinpointException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String internalName) {
        try {
            if (bootstrapCore.isBootstrapPackageByInternalName(internalName)) {
                return bootstrapCore.openStream(internalName);
            }
            if (targetClassLoader == null) {
                return bootstrapClassLoaderHandler.getResourceAsStream(null, internalName);
            } else if (targetClassLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) targetClassLoader;
                return urlClassLoaderHandler.getResourceAsStream(urlClassLoader, internalName);
            } else {
                return plainClassLoaderHandler.getResourceAsStream(targetClassLoader, internalName);
            }
        } catch (Throwable e) {
             logger.warn("Failed to load plugin resource as stream {} with classLoader {}", internalName, targetClassLoader, e);
            return null;
        }
    }
}