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
import com.navercorp.pinpoint.common.util.CodeSourceUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

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
        final URL pluginURL = CodeSourceUtils.getCodeLocation(plugin.getClass());
        if (pluginURL == null) {
            throw new IllegalStateException("pluginURL not found plugin:" + plugin.getClass());
        }

        final File file = new File(pluginURL.getFile());
        if (file.isDirectory()) {
            return new DirClassPathPlugin<T>(pluginURL, Collections.singletonList(plugin), Collections.<String>emptyList());
        }

        if (file.getName().endsWith(".jar")) {
            PluginJar pluginJar = PluginJar.fromFile(file);
            return new JarPlugin<T>(pluginJar, Collections.singletonList(plugin), Collections.<String>emptyList());
        }
        throw new IllegalArgumentException("unknown plugin " + pluginURL);
    }
}
