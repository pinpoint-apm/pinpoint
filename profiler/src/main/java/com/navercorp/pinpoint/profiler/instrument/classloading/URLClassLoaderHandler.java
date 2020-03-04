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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class URLClassLoaderHandler implements ClassInjector {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final Method ADD_URL;

    static {
        try {
            ADD_URL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            ADD_URL.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access URLClassLoader.addURL(URL)", e);
        }
    }

    private final PluginConfig pluginConfig;

    public URLClassLoaderHandler(PluginConfig pluginConfig) {
        this.pluginConfig = Assert.requireNonNull(pluginConfig, "pluginConfig");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        try {
            if (classLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                addPluginURLIfAbsent(urlClassLoader);
                return (Class<T>) urlClassLoader.loadClass(className);
            }
        } catch (Exception e) {
            logger.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new PinpointException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
        throw new PinpointException("invalid ClassLoader");
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String internalName) {
        try {
            if (targetClassLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) targetClassLoader;
                addPluginURLIfAbsent(urlClassLoader);
                return targetClassLoader.getResourceAsStream(internalName);
            }
        } catch (Exception e) {
            logger.warn("Failed to load plugin resource as stream {} with classLoader {}", internalName, targetClassLoader, e);
            return null;
        }
        return null;
    }

    private void addPluginURLIfAbsent(URLClassLoader classLoader) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final URL[] urls = classLoader.getURLs();
        if (urls != null) {
            final boolean hasPluginJar = hasPluginJar(urls);
            if (!hasPluginJar) {
                if (isDebug) {
                    logger.debug("add Jar:{}", pluginConfig.getPluginJarURLExternalForm());
                }
                ADD_URL.invoke(classLoader, pluginConfig.getPluginUrl());
            }
        }
    }

    private boolean hasPluginJar(URL[] urls) {
        for (URL url : urls) {
            // if (url.equals(pluginJarURL)) { fix very slow
            // http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html
            final String externalForm = url.toExternalForm();
            if (pluginConfig.getPluginJarURLExternalForm().equals(externalForm)) {
                return true;
            }
        }
        return false;
    }
}
