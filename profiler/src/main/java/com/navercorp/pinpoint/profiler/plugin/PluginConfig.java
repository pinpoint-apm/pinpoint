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

import com.navercorp.pinpoint.common.plugin.JarPlugin;
import com.navercorp.pinpoint.common.plugin.Plugin;
import com.navercorp.pinpoint.common.util.Assert;


import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginConfig {

    public static final String PINPOINT_PLUGIN_ID = "Pinpoint-Plugin-Id";
    public static final String PINPOINT_PLUGIN_PACKAGE = "Pinpoint-Plugin-Package";
    public static final String DEFAULT_PINPOINT_PLUGIN_PACKAGE_NAME = "com.navercorp.pinpoint.plugin";

    private final Plugin plugin;
    private final JarFile pluginJar;

    private String pluginJarURLExternalForm;

    private final ClassNameFilter pluginPackageFilter;

    public PluginConfig(Plugin plugin, ClassNameFilter pluginPackageFilter) {
        this.plugin = Assert.requireNonNull(plugin, "plugin must not be null");

        this.pluginPackageFilter = pluginPackageFilter;
        this.pluginJar = getJarFile(plugin);

    }

    private JarFile getJarFile(Plugin plugin) {
        if (plugin instanceof JarPlugin) {
            return ((JarPlugin) plugin).getJarFile();
        }
        throw new IllegalArgumentException("unsupported plugin " + plugin);
    }


    public URL getPluginJar() {
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