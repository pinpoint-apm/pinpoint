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
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.module.PluginJarPaths;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
import com.navercorp.pinpoint.profiler.plugin.filter.DefaultPluginFilterFactory;
import com.navercorp.pinpoint.profiler.plugin.filter.ImportPluginFilterFactory;
import com.navercorp.pinpoint.profiler.plugin.filter.PluginFilter;
import com.navercorp.pinpoint.profiler.plugin.filter.PluginFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class PluginJarsProvider implements Provider<List<PluginJar>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<PluginJar> pluginJars;

    @Inject
    public PluginJarsProvider(@PluginJarPaths List<String> pluginJarPaths, ProfilerConfig profilerConfig) {
        Assert.requireNonNull(pluginJarPaths, "pluginJarPaths");
        Assert.requireNonNull(profilerConfig, "profilerConfig");
        PluginFilter pluginFilter = createPluginJarFilter(profilerConfig);
        logger.info("pluginJarFilter:{}", pluginFilter);
        List<PluginJar> pluginJars = createPluginJars(pluginJarPaths, pluginFilter, profilerConfig.getPluginLoadOrder());
        this.pluginJars = Collections.unmodifiableList(pluginJars);
    }

    @Override
    public List<PluginJar> get() {
        return pluginJars;
    }

    private List<PluginJar> createPluginJars(final List<String> pluginJarPaths,
                                             final PluginFilter pluginFilter,
                                             final List<String> orderedPluginIdList) {
        if (CollectionUtils.isEmpty(pluginJarPaths)) {
            return Collections.emptyList();
        }
        Map<String, PluginJar> orderedPlugins = new LinkedHashMap<String, PluginJar>();
        if (CollectionUtils.hasLength(orderedPluginIdList)) {
            for (String orderedPluginId : orderedPluginIdList) {
                orderedPlugins.put(orderedPluginId, null);
            }
        }

        List<PluginJar> unorderedPlugins = new ArrayList<PluginJar>(pluginJarPaths.size());
        for (String pluginJarPath : pluginJarPaths) {
            PluginJar pluginJar = PluginJar.fromFilePath(pluginJarPath);
            if (!pluginFilter.accept(pluginJar)) {
                continue;
            }
            String pluginId = pluginJar.getPluginId();
            if (orderedPlugins.containsKey(pluginId)) {
                PluginJar prev = orderedPlugins.put(pluginId, pluginJar);
                if (prev != null) {
                    throw new IllegalStateException("Duplicate order found for plugin : " + pluginId);
                }
            } else {
                unorderedPlugins.add(pluginJar);
            }
        }
        List<PluginJar> pluginJars = new ArrayList<PluginJar>();
        for (PluginJar orderedPlugin : orderedPlugins.values()) {
            if (orderedPlugin != null) {
                pluginJars.add(orderedPlugin);
            }
        }
        pluginJars.addAll(unorderedPlugins);
        return pluginJars;
    }
    // ArtifactIdUtils.ARTIFACT_SEPARATOR
    private static final String ARTIFACT_SEPARATOR = ";";
    private PluginFilter createPluginJarFilter(ProfilerConfig profilerConfig) {
        final String importPluginIdString = profilerConfig.readString(DefaultProfilerConfig.IMPORT_PLUGIN, null);
        if (StringUtils.hasLength(importPluginIdString)) {
            List<String> importPluginIds = StringUtils.tokenizeToStringList(importPluginIdString, ARTIFACT_SEPARATOR);
            PluginFilterFactory filterFactory = new ImportPluginFilterFactory(importPluginIds);
            return filterFactory.newPluginFilter();
        }

        PluginFilterFactory filterFactory = new DefaultPluginFilterFactory(profilerConfig);
        return filterFactory.newPluginFilter();
    }

}
