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
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.context.module.PluginJarPaths;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
import com.navercorp.pinpoint.profiler.plugin.config.PluginLoadingConfig;
import com.navercorp.pinpoint.profiler.plugin.filter.DefaultPluginFilterFactory;
import com.navercorp.pinpoint.profiler.plugin.filter.ImportPluginFilterFactory;
import com.navercorp.pinpoint.profiler.plugin.filter.PluginFilter;
import com.navercorp.pinpoint.profiler.plugin.filter.PluginFilterFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class PluginJarsProvider implements Provider<List<PluginJar>> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final List<PluginJar> pluginJars;

    @Inject
    public PluginJarsProvider(@PluginJarPaths List<String> pluginJarPaths, PluginLoadingConfig pluginLoadingConfig) {
        Objects.requireNonNull(pluginJarPaths, "pluginJarPaths");
        Objects.requireNonNull(pluginLoadingConfig, "pluginLoadingConfig");

        PluginFilter pluginFilter = createPluginJarFilter(pluginLoadingConfig);
        logger.info("pluginJarFilter:{}", pluginFilter);
        List<PluginJar> pluginJars = createPluginJars(pluginJarPaths, pluginFilter, pluginLoadingConfig.getPluginLoadOrder());
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
        List<PluginJar> pluginList = filter(pluginJarPaths, pluginFilter);

        return sort(pluginList, orderedPluginIdList);
    }

    private List<PluginJar> sort(List<PluginJar> pluginList, List<String> orderedPluginIdList) {
        Map<String, PluginJar> orderedPlugins = getOrderedPlugins(orderedPluginIdList);

        List<PluginJar> unorderedPlugins = new ArrayList<>();
        for (PluginJar pluginJar : pluginList) {
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
        List<PluginJar> pluginJars = new ArrayList<>();
        for (PluginJar orderedPlugin : orderedPlugins.values()) {
            if (orderedPlugin != null) {
                pluginJars.add(orderedPlugin);
            }
        }
        pluginJars.addAll(unorderedPlugins);
        return pluginJars;
    }

    private List<PluginJar> filter(List<String> pluginJarPaths, PluginFilter pluginFilter) {
        List<PluginJar> list = new ArrayList<>();
        for (String pluginJarPath : pluginJarPaths) {
            PluginJar pluginJar = PluginJar.fromFilePath(pluginJarPath);
            if (!pluginFilter.accept(pluginJar)) {
                continue;
            }
            list.add(pluginJar);
        }
        return list;
    }

    private Map<String, PluginJar> getOrderedPlugins(List<String> orderedPluginIdList) {
        if (CollectionUtils.isEmpty(orderedPluginIdList)) {
            return new LinkedHashMap<>();
        }

        Map<String, PluginJar> orderedPlugins = new LinkedHashMap<>();
        for (String orderedPluginId : orderedPluginIdList) {
            orderedPlugins.put(orderedPluginId, null);
        }
        return orderedPlugins;
    }


    private PluginFilter createPluginJarFilter(PluginLoadingConfig pluginLoadingConfig) {
        List<String> importPluginIds = pluginLoadingConfig.getImportPluginIds();
        if (CollectionUtils.hasLength(importPluginIds)) {
            PluginFilterFactory filterFactory = new ImportPluginFilterFactory(importPluginIds);
            return filterFactory.newPluginFilter();
        }

        PluginFilterFactory filterFactory = new DefaultPluginFilterFactory(pluginLoadingConfig);
        return filterFactory.newPluginFilter();
    }

}
