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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.plugin.PluginJar;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.context.module.PluginJarPaths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class PluginJarsProvider implements Provider<List<PluginJar>> {

    private List<PluginJar> pluginJars;

    @Inject
    public PluginJarsProvider(@PluginJarPaths List<String> pluginJarPaths, ProfilerConfig profilerConfig) {
        Assert.requireNonNull(pluginJarPaths, "pluginJarPaths must not be null");
        List<PluginJar> pluginJars = createPluginJars(pluginJarPaths);
        List<PluginJar> orderedPluginJars = orderPluginJars(pluginJars, profilerConfig.getPluginLoadOrder());
        this.pluginJars = ImmutableList.copyOf(orderedPluginJars);
    }

    @Override
    public List<PluginJar> get() {
        return pluginJars;
    }

    private List<PluginJar> createPluginJars(List<String> pluginJarPaths) {
        List<PluginJar> pluginJars = new ArrayList<PluginJar>(pluginJarPaths.size());
        for (String pluginJarPath : pluginJarPaths) {
            pluginJars.add(PluginJar.fromFilePath(pluginJarPath));
        }
        return pluginJars;
    }

    private List<PluginJar> orderPluginJars(List<PluginJar> pluginJars, final List<String> orderedPluginIdList) {
        if (CollectionUtils.isEmpty(pluginJars)) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(orderedPluginIdList)) {
            return pluginJars;
        }

        Map<String, PluginJar> orderedPlugins = new LinkedHashMap<String, PluginJar>();
        for (String orderedPluginId : orderedPluginIdList) {
            orderedPlugins.put(orderedPluginId, null);
        }

        List<PluginJar> result = new ArrayList<PluginJar>(pluginJars.size());
        for (PluginJar pluginJar : pluginJars) {
            String pluginId = pluginJar.getPluginId();
            if (orderedPlugins.containsKey(pluginId)) {
                orderedPlugins.put(pluginId, pluginJar);
            } else {
                result.add(pluginJar);
            }
        }

        for (PluginJar orderedPlugin : orderedPlugins.values()) {
            if (orderedPlugin != null) {
                result.add(orderedPlugin);
            }
        }
        return result;
    }
}
