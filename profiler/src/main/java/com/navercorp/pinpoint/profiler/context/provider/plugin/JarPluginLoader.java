/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.plugin;

import com.navercorp.pinpoint.common.plugin.JarPlugin;
import com.navercorp.pinpoint.common.plugin.Plugin;
import com.navercorp.pinpoint.common.plugin.PluginJar;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClassLoader parentClassLoader;

    private final List<PluginJar> pluginJars;

    public JarPluginLoader(List<PluginJar> pluginJars, ClassLoader parentClassLoader) {
        //@Nullable
        this.parentClassLoader = parentClassLoader;
        this.pluginJars = pluginJars;
    }

    private ClassLoader createPluginClassLoader(final URL[] urls, final ClassLoader parent) {
        if (logger.isDebugEnabled()) {
            logger.debug("createPluginClassLoader(urls = [{}], parent = [{}])", Arrays.toString(urls), parent);
        }
        if (SECURITY_MANAGER != null) {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return new URLClassLoader(urls, parent);
                }
            });
        } else {
            return new URLClassLoader(urls, parent);
        }
    }

    @Override
    public <T> List<Plugin<T>> load(Class<T> serviceType) {
        List<Plugin<T>> result = new ArrayList<Plugin<T>>();
        for (PluginJar pluginJar : pluginJars) {
            final Plugin<T> plugin = newPlugin(serviceType, pluginJar);
            result.add(plugin);
        }

        return result;
    }

    private <T> Plugin<T> newPlugin(Class<T> serviceType, PluginJar pluginJar) {
        URL pluginURL = pluginJar.getUrl();
        ClassLoader pluginClassLoader = createPluginClassLoader(new URL[]{pluginURL}, parentClassLoader);
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceType, pluginClassLoader);
        List<T> pluginList = toList(serviceLoader, serviceType);

        String pluginPackages = pluginJar.getPluginPackages();
        List<String> pluginPackageList = StringUtils.tokenizeToStringList(pluginPackages, ",");

        return new JarPlugin<T>(pluginJar, pluginList, pluginPackageList);
    }

    private static <T> List<T> toList(Iterable<T> iterable, Class<T> serviceType) {
        final List<T> list = new ArrayList<T>();
        for (T plugin : iterable) {
            list.add(serviceType.cast(plugin));
        }
        return  list;
    }
}
