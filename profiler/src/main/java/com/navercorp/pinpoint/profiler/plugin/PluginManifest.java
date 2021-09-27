/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.util.JarFileUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class PluginManifest {

    private final String pluginId;
    private final String pluginCompilerVersion;
    private final List<String> pluginPackages;

    public PluginManifest(String pluginId, String pluginCompilerVersion, List<String> pluginPackages) {
        this.pluginId = pluginId;
        this.pluginCompilerVersion = pluginCompilerVersion;
        this.pluginPackages = pluginPackages;
    }

    public static PluginManifest of(JarFile jarFile) {
        Objects.requireNonNull(jarFile, "jarFile");

        final Manifest manifest = getManifest(jarFile);
        final Attributes mainAttributes = manifest.getMainAttributes();

        String pluginId = JarFileUtils.getValue(mainAttributes, PluginJar.PINPOINT_PLUGIN_ID, null);
        String  pluginCompilerVersion = JarFileUtils.getValue(mainAttributes, PluginJar.PINPOINT_PLUGIN_COMPILER_VERSION, null);

        String pluginPackages = JarFileUtils.getValue(mainAttributes, PluginJar.PINPOINT_PLUGIN_PACKAGE, PluginJar.DEFAULT_PINPOINT_PLUGIN_PACKAGE_NAME);

        List<String> pluginPackageList = StringUtils.tokenizeToStringList(pluginPackages, ",");

        return new PluginManifest(pluginId, pluginCompilerVersion, pluginPackageList);
    }

    private static Manifest getManifest(JarFile jarFile) {
        try {
            return jarFile.getManifest();
        } catch (IOException e) {
            throw new PluginException(jarFile.getName() + " Manifest error", e);
        }
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getPluginCompilerVersion() {
        return pluginCompilerVersion;
    }

    public List<String> getPluginPackages() {
        return pluginPackages;
    }

    @Override
    public String toString() {
        return "PluginManifest{" +
                "pluginId='" + pluginId + '\'' +
                ", pluginCompilerVersion='" + pluginCompilerVersion + '\'' +
                ", pluginPackages=" + pluginPackages +
                '}';
    }
}
