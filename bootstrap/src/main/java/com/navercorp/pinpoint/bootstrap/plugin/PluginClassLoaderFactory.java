/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginClassLoaderFactory {

    private final Logger logger = Logger.getLogger(PluginClassLoaderFactory.class.getName());

    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();

    private final URL[] pluginJars;
    private final ConcurrentHashMap<ClassLoader, ClassLoader> cache = new ConcurrentHashMap<ClassLoader, ClassLoader>();
    
    public PluginClassLoaderFactory(URL[] pluginJars) {
        this.pluginJars = pluginJars;
    }
    
    public ClassLoader get(ClassLoader loader) { 
        final ClassLoader forPlugin = cache.get(loader);
        if (forPlugin != null) {
            return forPlugin;
        }
        
        final ClassLoader newInstance = createPluginClassLoader(pluginJars, loader);
        final ClassLoader before = cache.putIfAbsent(loader, newInstance);
        if (before == null) {
            return newInstance;
        }
        else {
            close (newInstance);
            return before;
        }
    }

    private PluginClassLoader createPluginClassLoader(final URL[] urls, final ClassLoader parent) {
        if (SECURITY_MANAGER != null) {
            return AccessController.doPrivileged(new PrivilegedAction<PluginClassLoader>() {
                public PluginClassLoader run() {
                    return new PluginClassLoader(urls, parent);
                }
            });
        } else {
            return new PluginClassLoader(urls, parent);
        }
    }

    private void close(ClassLoader classLoader) {
        if (classLoader == null) {
            return;
        }
        // after jdk 1.7
        if (classLoader instanceof Closeable) {
            try {
                ((Closeable)classLoader).close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "PluginClassLoader.close() fail. Caused:" + e.getMessage(), e);
            }
        }
    }

}
