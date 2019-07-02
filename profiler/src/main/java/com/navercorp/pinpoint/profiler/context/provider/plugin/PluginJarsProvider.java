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
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
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
        Assert.requireNonNull(pluginJarPaths, "pluginJarPaths must not be null");
        Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        PluginJarFilter pluginJarFilter = createPluginJarFilter(profilerConfig.getDisabledPlugins());
        List<PluginJar> pluginJars = createPluginJars(pluginJarPaths, pluginJarFilter, profilerConfig.getPluginLoadOrder());
        this.pluginJars = Collections.unmodifiableList(pluginJars);
    }

    @Override
    public List<PluginJar> get() {
        return pluginJars;
    }

    private List<PluginJar> createPluginJars(final List<String> pluginJarPaths,
                                             final PluginJarFilter pluginJarFilter,
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
            if (!pluginJarFilter.accept(pluginJar)) {
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

    private PluginJarFilter createPluginJarFilter(List<String> disabledPlugins) {
        PluginJarFilter javaVersionFilter = new JavaVersionFilter();
        if (CollectionUtils.isEmpty(disabledPlugins)) {
            return javaVersionFilter;
        }
        PluginJarFilter disabledPluginFilter = new DisabledPluginFilter(disabledPlugins);
        return new PluginJarFilters(disabledPluginFilter, javaVersionFilter);
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
            String pluginId = pluginJar.getPluginId();
            if (pluginId == null) {
                logger.warn("Invalid plugin : {}, missing manifest entry : {}", pluginJar.getJarFile().getName(), PluginJar.PINPOINT_PLUGIN_ID);
                return REJECT;
            }
            String pluginCompilerVersion = pluginJar.getPluginCompilerVersion();
            if (pluginCompilerVersion == null) {
                logger.info("Skipping {} due to missing manifest entry : {}", pluginJar.getJarFile().getName(), PluginJar.PINPOINT_PLUGIN_COMPILER_VERSION);
                return REJECT;
            }
            JvmVersion pluginJvmVersion = JvmVersion.getFromVersion(pluginCompilerVersion);
            if (pluginJvmVersion == JvmVersion.UNSUPPORTED) {
                logger.info("Skipping {} due to unknown plugin compiler version : {}", pluginCompilerVersion);
                return REJECT;
            }
            if (jvmVersion.onOrAfter(pluginJvmVersion)) {
                return ACCEPT;
            }
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
