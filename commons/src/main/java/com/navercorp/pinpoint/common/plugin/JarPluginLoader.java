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

package com.navercorp.pinpoint.common.plugin;

import com.navercorp.pinpoint.common.util.Assert;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * TODO Loading all plugins with a single class loader could cause class collisions.
 * Also, with current implementation, plugins can use dependencies by putting them in the plugin directory too.
 * But it can lead to dependency collision between plugins because they are loaded by a single class loader.
 * <p>
 * How can we prevent this?
 * A ClassLoader per plugin could do it but then we have to create "N of target class loader" x "N of plugin" class loaders.
 * It seems too much. For now, Just leave it as it is.
 *
 * @author Jongho Moon <jongho.moon@navercorp.com>
 * @author emeroad
 */
public class JarPluginLoader implements PluginLoader {
    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();

    private final ClassLoader parentClassLoader;

    private final Map<URL, ClassLoader> map;

    public JarPluginLoader(URL[] pluginJar, ClassLoader parentClassLoader) {
        this.map = loadPluginJar(pluginJar);
        this.parentClassLoader = Assert.requireNonNull(parentClassLoader, "parentClassLoader must not be null");
    }

    private Map<URL, ClassLoader> loadPluginJar(URL[] pluginJar) {
        Assert.requireNonNull(pluginJar, "pluginJar must not be null");
        // considering cl shared policy
        return isolationPolicy(pluginJar);
    }

    private Map<URL, ClassLoader> isolationPolicy(URL[] pluginJar) {
        final Map<URL, ClassLoader> map = new HashMap<URL, ClassLoader>();
        for (URL url : pluginJar) {
            final ClassLoader pluginClassLoader = createPluginClassLoader(new URL[]{url}, parentClassLoader);
            map.put(url, pluginClassLoader);
        }
        return map;
    }

//    private Map<URL, ClassLoader> sharedPolicy(URL[] pluginJar) {
//        final ClassLoader sharedClassLoader = createPluginClassLoader(pluginJar, parentClassLoader);
//        final Map<URL, ClassLoader> map = new HashMap<URL, ClassLoader>();
//        for (URL url : pluginJar) {
//            map.put(url, sharedClassLoader);
//        }
//        return map;
//    }

    private ClassLoader createPluginClassLoader(final URL[] urls, final ClassLoader parent) {
        if (SECURITY_MANAGER != null) {
            return AccessController.doPrivileged(new PrivilegedAction<PluginLoaderClassLoader>() {
                public PluginLoaderClassLoader run() {
                    return new PluginLoaderClassLoader(urls, parent);
                }
            });
        } else {
            return new PluginLoaderClassLoader(urls, parent);
        }
    }

    @Override
    public <T> List<T> load(URL pluginUrl, Class<T> serviceType) {
        final ServiceLoader<T> serviceLoader = getServiceLoader(pluginUrl, serviceType);
        return toList(serviceLoader, serviceType);
    }

    private static <T> List<T> toList(Iterable<T> iterable, Class<T> serviceType) {
        final List<T> list = new ArrayList<T>();
        for (T plugin : iterable) {
            list.add(serviceType.cast(plugin));
        }
        return  list;
    }

    private <T> ServiceLoader<T> getServiceLoader(URL pluginUrl, Class<T> serviceType) {
        final ClassLoader classLoader = map.get(pluginUrl);
        if (classLoader == null) {
            throw new IllegalArgumentException(pluginUrl + " not found");
        }
        return ServiceLoader.load(serviceType, classLoader);
    }
}
