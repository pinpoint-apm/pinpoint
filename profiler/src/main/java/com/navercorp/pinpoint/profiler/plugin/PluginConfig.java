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

import com.navercorp.pinpoint.common.util.Assert;


import java.net.URL;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginConfig {

    private final Plugin<?> plugin;
    private final JarFile pluginJar;
    private final ClassNameFilter pluginPackageFilter;

    private String pluginJarURLExternalForm;

    public PluginConfig(Plugin<?> plugin, ClassNameFilter pluginPackageFilter) {
        this.plugin = Assert.requireNonNull(plugin, "plugin");
        this.pluginPackageFilter = pluginPackageFilter;
        this.pluginJar = getJarFile(plugin);
    }

    private JarFile getJarFile(Plugin<?> plugin) {
        if (plugin instanceof JarPlugin) {
            return ((JarPlugin) plugin).getJarFile();
        }
        throw new IllegalArgumentException("unsupported plugin " + plugin);
    }

    public URL getPluginUrl() {
        return plugin.getURL();
    }

    public JarFile getPluginJarFile() {
        return pluginJar;
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
                "pluginJar=" + plugin.getURL() +
                ", pluginJarURLExternalForm='" + pluginJarURLExternalForm + '\'' +
                ", pluginPackageFilter=" + pluginPackageFilter +
                '}';
    }
}