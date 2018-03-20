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

package com.navercorp.pinpoint.common.plugin;

import com.navercorp.pinpoint.common.util.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServerPluginLoader implements PluginLoader {

    private final ClassLoader classLoader;

    public ServerPluginLoader(ClassLoader classLoader) {
        this.classLoader = Assert.requireNonNull(classLoader, "classLoader must not be null");
    }

    @Override
    public <T> List<Plugin<T>> load(Class<T> serviceType) {
        Assert.requireNonNull(serviceType, "serviceType must not be null");

        List<Plugin<T>> pluginList = new ArrayList<Plugin<T>>();
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceType, classLoader);

        for (T plugin : serviceLoader) {
            Plugin<T> simple = newPlugin(plugin);
            pluginList.add(simple);
        }

        return pluginList;
    }

    private <T> Plugin<T> newPlugin(T plugin) {
        URL pluginURL = getPluginUrl(plugin);

        final File file = new File(pluginURL.getPath());
        if (file.isDirectory()) {
            return new DirClassPathPlugin<T>(pluginURL, Collections.singletonList(plugin), Collections.<String>emptyList());
        }

        if (file.getName().endsWith(".jar")) {
            JarFile jarFile = toJarFile(file);
            return new JarPlugin<T>(pluginURL, jarFile, Collections.singletonList(plugin), Collections.<String>emptyList());
        }
        throw new IllegalArgumentException("unknown plugin " + pluginURL);
    }

    private <T> URL getPluginUrl(T plugin) {
        ProtectionDomain protectionDomain = plugin.getClass().getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        return codeSource.getLocation();
    }

    private JarFile toJarFile(File file) {
        try {
            return new JarFile(file);
        } catch (IOException e) {
            throw new RuntimeException("jarFile create fail " + e.getMessage(), e);
        }
    }
}
