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

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * TODO Loading all plugins with a single class loader could cause class collisions.
 *      Also, with current implementation, plugins can use dependencies by putting them in the plugin directory too.
 *      But it can lead to dependency collision between plugins because they are loaded by a single class loader.
 *      
 *      How can we prevent this?
 *      A ClassLoader per plugin could do it but then we have to create "N of target class loader" x "N of plugin" class loaders.
 *      It seems too much. For now, Just leave it as it is. 
 * 
 * 
 * @author Jongho Moon <jongho.moon@navercorp.com>
 * @author emeroad
 *
 * @param <T>
 */
public class PluginLoader<T> {

    private static final URL[] EMPTY_URL = new URL[0];
    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();

    public static <T> Plugins<T> load(Class<T> serviceType, String pluginPath) {
        URL[] jars = findJars(pluginPath);
        URLClassLoader classLoader = createPluginClassLoader(jars, ClassLoader.getSystemClassLoader());
        List<T> plugins = load(serviceType, classLoader);
        
        return new Plugins<T>(plugins, jars);
    }

    private static PluginClassLoader createPluginClassLoader(final URL[] urls, final ClassLoader parent) {
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
    
    public static <T> List<T> load(Class<T> serviceType, ClassLoader classLoader) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceType, classLoader);
        
        List<T> plugins = new ArrayList<T>();
        for (T plugin : serviceLoader) {
            plugins.add(plugin);
        }

        return plugins;
    }
    
    private static URL[] findJars(String pluginPath) {
        final File file = new File(pluginPath);
        
        if (!file.exists() || !file.isDirectory()) {
            return EMPTY_URL;
        }
        
        final File[] jars = file.listFiles(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (jars == null || jars.length == 0) {
            return EMPTY_URL;
        }
        
        final URL[] urls = new URL[jars.length];
        
        for (int i = 0; i < jars.length; i++) {
            try {
                urls[i] = jars[i].toURI().toURL();
            } catch (MalformedURLException e) {
                // TODO have to change to PinpointException after moving the exception to pinpint-common
                throw new RuntimeException("Fail to load plugin jars", e);
            }
        }
        
        return urls;
    }
}
