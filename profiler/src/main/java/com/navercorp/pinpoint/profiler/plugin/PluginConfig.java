/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.plugin;

import java.net.URL;
import java.util.Objects;


import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginConfig {

    private final JarPlugin<?> plugin;
    private final ClassNameFilter pluginPackageFilter;

    private String pluginJarURLExternalForm;

    public PluginConfig(Plugin<?> plugin, ClassNameFilter pluginPackageFilter) {
        this.plugin = cast(plugin);
        this.pluginPackageFilter = pluginPackageFilter;
    }

    private JarPlugin<?> cast(Plugin<?> plugin) {
        Objects.requireNonNull(plugin, "plugin");

        if (plugin instanceof JarPlugin) {
            return (JarPlugin<?>) plugin;
        }
        throw new PluginException("unsupported plugin " + plugin);
    }

    public URL getPluginURL() {
        return plugin.getURL();
    }

    public JarFile getPluginJarFile() {
        return plugin.getJarFile();
    }

    public String getPluginJarURLExternalForm() {
        if (this.pluginJarURLExternalForm == null) {
            this.pluginJarURLExternalForm = plugin.getURL().toExternalForm();
        }
        return this.pluginJarURLExternalForm;
    }

    public ClassNameFilter getPluginPackageFilter() {
        return pluginPackageFilter;
    }

    @Override
    public String toString() {
        return "PluginConfig{" +
                "pluginJar=" + pluginJarURLExternalForm +
                ", pluginPackageFilter=" + pluginPackageFilter +
                '}';
    }
}