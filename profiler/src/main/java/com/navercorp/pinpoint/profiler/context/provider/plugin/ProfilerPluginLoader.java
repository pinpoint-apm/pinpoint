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
package com.navercorp.pinpoint.profiler.context.provider.plugin;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.common.plugin.Plugin;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjectorFactory;
import com.navercorp.pinpoint.profiler.plugin.ClassNameFilter;
import com.navercorp.pinpoint.profiler.plugin.ClassNameFilterChain;
import com.navercorp.pinpoint.profiler.plugin.PinpointProfilerPackageSkipFilter;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import com.navercorp.pinpoint.profiler.plugin.PluginPackageFilter;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.plugin.SetupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jongho Moon
 *
 */
public class ProfilerPluginLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ClassNameFilter profilerPackageFilter = new PinpointProfilerPackageSkipFilter();

    private final ProfilerConfig profilerConfig;
    private final PluginSetup pluginSetup;
    private final ClassInjectorFactory classInjectorFactory;
    private final PluginLoader pluginLoader;


    public ProfilerPluginLoader(ProfilerConfig profilerConfig, PluginSetup pluginSetup,
                                ClassInjectorFactory classInjectorFactory, PluginLoader pluginLoader) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.pluginSetup = Assert.requireNonNull(pluginSetup, "pluginSetup must not be null");
        this.classInjectorFactory = Assert.requireNonNull(classInjectorFactory, "classInjectorFactory must not be null");
        this.pluginLoader = Assert.requireNonNull(pluginLoader, "pluginLoader must not be null");

    }

    public List<SetupResult> load() {

        List<Plugin<ProfilerPlugin>> plugins = pluginLoader.load(ProfilerPlugin.class);

        List<SetupResult> pluginContexts = new ArrayList<SetupResult>(plugins.size());
        for (Plugin<ProfilerPlugin> plugin : plugins) {
            List<SetupResult> setupResults = loadProfilerPlugin(plugin);
            pluginContexts.addAll(setupResults);
        }

        return pluginContexts;
    }

    private List<SetupResult> loadProfilerPlugin(Plugin<ProfilerPlugin> plugin) {
        List<String> pluginPackageList = plugin.getPackageList();
        final ClassNameFilter pluginFilterChain = createPluginFilterChain(pluginPackageList);

        List<ProfilerPlugin> filterProfilerPlugin = filterProfilerPlugin(plugin.getInstanceList(), profilerConfig.getDisabledPlugins());

        List<SetupResult> result = new ArrayList<SetupResult>();
        for (ProfilerPlugin profilerPlugin : filterProfilerPlugin) {
            if (logger.isInfoEnabled()) {
                logger.info("{} Plugin {}:{}", profilerPlugin.getClass(), PluginConfig.PINPOINT_PLUGIN_PACKAGE, pluginPackageList);
                logger.info("Loading plugin:{} pluginPackage:{}", profilerPlugin.getClass().getName(), profilerPlugin);
            }

            PluginConfig pluginConfig = new PluginConfig(plugin, pluginFilterChain);
            final ClassInjector classInjector = classInjectorFactory.newClassInjector(pluginConfig);
            final SetupResult setupResult = pluginSetup.setupPlugin(profilerPlugin, classInjector);
            result.add(setupResult);
        }
        return result;
    }

    private List<ProfilerPlugin> filterProfilerPlugin(List<ProfilerPlugin> originalProfilerPlugin, List<String> disabled) {
        List<ProfilerPlugin> result = new ArrayList<ProfilerPlugin>();
        for (ProfilerPlugin profilerPlugin : originalProfilerPlugin) {
            if (disabled.contains(profilerPlugin.getClass().getName())) {
                logger.info("Skip disabled plugin: {}", profilerPlugin.getClass().getName());
                continue;
            }
            result.add(profilerPlugin);
        }
        return result;
    }

    private ClassNameFilter createPluginFilterChain(List<String> packageList) {

        final ClassNameFilter pluginPackageFilter = new PluginPackageFilter(packageList);

        final List<ClassNameFilter> chain = Arrays.asList(profilerPackageFilter, pluginPackageFilter);

        final ClassNameFilter filterChain = new ClassNameFilterChain(chain);

        return filterChain;
    }

}
