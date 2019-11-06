/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.plugin;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
import com.navercorp.pinpoint.profiler.context.module.PluginJars;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class PluginClassLoaderProvider implements Provider<ClassLoader> {

    private final ClassLoader pluginClassLoader;

    @Inject
    public PluginClassLoaderProvider(@PluginJars List<PluginJar> pluginJars) {
        // TODO configuration support
        ClassLoader parentClassLoader = Object.class.getClassLoader();
        List<URL> pluginUrls = new ArrayList<URL>(pluginJars.size());
        for (PluginJar pluginJar : pluginJars) {
            pluginUrls.add(pluginJar.getUrl());
        }
        this.pluginClassLoader = createPluginClassLoader(pluginUrls, parentClassLoader);
    }

    private ClassLoader createPluginClassLoader(List<URL> pluginUrls, final ClassLoader parentClassLoader) {
        final URL[] urls = pluginUrls.toArray(new URL[0]);
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    return new URLClassLoader(urls, parentClassLoader);
                }
            });
        }
        return new URLClassLoader(urls, parentClassLoader);
    }

    @Override
    public ClassLoader get() {
        return pluginClassLoader;
    }
}
