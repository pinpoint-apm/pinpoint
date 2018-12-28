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
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.context.module.PluginJarPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class PluginJarsProvider implements Provider<List<PluginJar>> {

    private final List<PluginJar> pluginJars;

    @Inject
    public PluginJarsProvider(@PluginJarPaths List<String> pluginJarPaths, ProfilerConfig profilerConfig) {
        Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        Assert.requireNonNull(pluginJarPaths, "pluginJarPaths must not be null");
        List<PluginJar> pluginJars = createPluginJars(pluginJarPaths);
        pluginJars = filterPluginJars(profilerConfig, pluginJars);
        pluginJars = orderPluginJars(profilerConfig, pluginJars);
        this.pluginJars = ImmutableList.copyOf(pluginJars);
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

    private List<PluginJar> filterPluginJars(ProfilerConfig profilerConfig, List<PluginJar> pluginJars) {
        if (CollectionUtils.isEmpty(pluginJars)) {
            return Collections.emptyList();
        }
        PluginJarFilter pluginJarFilter = createPluginJarFilter(profilerConfig.getDisabledPlugins());
        List<PluginJar> filteredPluginJars = new ArrayList<PluginJar>();
        for (PluginJar pluginJar : pluginJars) {
            if (pluginJarFilter.accept(pluginJar)) {
                filteredPluginJars.add(pluginJar);
            }
        }
        return filteredPluginJars;
    }

    private PluginJarFilter createPluginJarFilter(List<String> disabledPlugins) {
        PluginJarFilter javaVersionFilter = new JavaVersionFilter();
        if (CollectionUtils.isEmpty(disabledPlugins)) {
            return javaVersionFilter;
        }
        PluginJarFilter disabledPluginFilter = new DisabledPluginFilter(disabledPlugins);
        return new PluginJarFilters(disabledPluginFilter, javaVersionFilter);
    }

    private List<PluginJar> orderPluginJars(ProfilerConfig profilerConfig, List<PluginJar> pluginJars) {
        if (CollectionUtils.isEmpty(pluginJars)) {
            return Collections.emptyList();
        }
        List<String> orderedPluginIdList = profilerConfig.getPluginLoadOrder();
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
                PluginJar prev = orderedPlugins.put(pluginId, pluginJar);
                if (prev != null) {
                    throw new IllegalStateException("Duplicate order found for plugin : " + pluginId);
                }
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

    private interface PluginJarFilter {
        boolean ACCEPT = true;
        boolean REJECT = false;

        boolean accept(PluginJar pluginJar);
    }

    private static class PluginJarFilters implements PluginJarFilter {

        private final List<PluginJarFilter> pluginJarFilters = new ArrayList<PluginJarFilter>();

        private PluginJarFilters(PluginJarFilter pluginJarFilter, PluginJarFilter... pluginJarFilters) {
            Assert.requireNonNull(pluginJarFilter, "pluginJarFilter must not be null");
            this.pluginJarFilters.add(pluginJarFilter);
            if (pluginJarFilters.length > 0) {
                this.pluginJarFilters.addAll(Arrays.asList(pluginJarFilters));
            }
        }

        @Override
        public boolean accept(PluginJar pluginJar) {
            for (PluginJarFilter pluginJarFilter : pluginJarFilters) {
                if (pluginJarFilter.accept(pluginJar) == REJECT) {
                    return REJECT;
                }
            }
            return ACCEPT;
        }
    }

    private static class JavaVersionFilter implements PluginJarFilter {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private final JvmVersion jvmVersion = JvmUtils.getVersion();

        @Override
        public boolean accept(PluginJar pluginJar) {
            String pluginCompilerVersion = pluginJar.getPluginCompilerVersion();
            JvmVersion pluginJvmVersion = JvmVersion.getFromVersion(pluginCompilerVersion);
            if (jvmVersion.onOrAfter(pluginJvmVersion)) {
                return ACCEPT;
            }
            String pluginId = pluginJar.getPluginId();
            logger.info("Skipping {} due to java version. Required : {}, found : {}", pluginId, pluginJvmVersion, jvmVersion);
            return REJECT;
        }
    }

    private static class DisabledPluginFilter implements PluginJarFilter {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private final Set<String> disabledPluginIds;

        private DisabledPluginFilter(List<String> disabledPluginIds) {
            Assert.requireNonNull(disabledPluginIds, "disabledPluginIds must not be null");
            this.disabledPluginIds = new HashSet<String>(disabledPluginIds);
        }

        @Override
        public boolean accept(PluginJar pluginJar) {
            String pluginId = pluginJar.getPluginId();
            if (disabledPluginIds.contains(pluginId)) {
                logger.info("Skipping disabled plugin : {}", pluginId);
                return REJECT;
            }
            return ACCEPT;
        }
    }
}
