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
package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginGlobalContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jongho Moon
 *
 */
public class DefaultProfilerPluginContextLoader implements ProfilerPluginContextLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ClassNameFilter profilerPackageFilter = new PinpointProfilerPackageSkipFilter();

    private final ProfilerConfig profilerConfig;
    private final ServiceType configuredApplicationType;
    private final ClassInjectorFactory classInjectorFactory;
    private final PluginSetup pluginSetup;
    private final List<PluginJar> pluginJars;

    public DefaultProfilerPluginContextLoader(ProfilerConfig profilerConfig, ServiceType configuredApplicationType,
                                              ClassInjectorFactory classInjectorFactory, PluginSetup pluginSetup,
                                              List<PluginJar> pluginJars) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.configuredApplicationType = Assert.requireNonNull(configuredApplicationType, "configuredApplicationType");
        this.classInjectorFactory = Assert.requireNonNull(classInjectorFactory, "classInjectorFactory");
        this.pluginSetup = Assert.requireNonNull(pluginSetup, "pluginSetup");
        this.pluginJars = Assert.requireNonNull(pluginJars, "pluginJars");
    }

    @Override
    public PluginsSetupResult load(List<ProfilerPlugin> profilerPlugins) {
        ProfilerPluginGlobalContext globalContext = new DefaultProfilerPluginGlobalContext(profilerConfig, configuredApplicationType);
        PluginsSetupResult pluginsSetupResult = new PluginsSetupResult();
        JarPluginComponents jarPluginComponents = new JarPluginComponents(pluginJars);
        for (ProfilerPlugin profilerPlugin : profilerPlugins) {
            jarPluginComponents.addProfilerPlugin(profilerPlugin);
        }
        Iterable<JarPlugin<ProfilerPlugin>> jarPlugins = jarPluginComponents.buildJarPlugins();
        for (JarPlugin<ProfilerPlugin> jarPlugin : jarPlugins) {
            List<PluginSetupResult> setupResults = setupPlugin(globalContext, jarPlugin);
            pluginsSetupResult.addPluginSetupResults(setupResults);
        }
        ServiceType detectedApplicationType = globalContext.getApplicationType();
        pluginsSetupResult.setApplicationType(detectedApplicationType);

        return pluginsSetupResult;
    }

    private List<PluginSetupResult> setupPlugin(ProfilerPluginGlobalContext globalContext, JarPlugin<ProfilerPlugin> plugin) {
        List<String> pluginPackageList = plugin.getPackageList();
        final ClassNameFilter pluginFilterChain = createPluginFilterChain(pluginPackageList);

        List<ProfilerPlugin> filterProfilerPlugin = filterProfilerPlugin(plugin.getInstanceList(), profilerConfig.getDisabledPlugins());

        List<PluginSetupResult> result = new ArrayList<PluginSetupResult>();
        for (ProfilerPlugin profilerPlugin : filterProfilerPlugin) {
            if (logger.isInfoEnabled()) {
                logger.info("{} Plugin {}:{}", profilerPlugin.getClass(), PluginJar.PINPOINT_PLUGIN_PACKAGE, pluginPackageList);
                logger.info("Loading plugin:{} pluginPackage:{}", profilerPlugin.getClass().getName(), profilerPlugin);
            }

            PluginConfig pluginConfig = new PluginConfig(plugin, pluginFilterChain);
            final ClassInjector classInjector = classInjectorFactory.newClassInjector(pluginConfig);
            final PluginSetupResult setupResult = pluginSetup.setupPlugin(globalContext, profilerPlugin, classInjector);
            result.add(setupResult);
        }
        return result;
    }

    // 1.9.0 - Disabled plugin configuration should now be groupId:artifactId
    @Deprecated
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

    private static class JarPluginComponents {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final Map<String, JarPluginComponent> componentMap;

        private JarPluginComponents(List<PluginJar> pluginJars) {
            this.componentMap = new LinkedHashMap<String, JarPluginComponent>(pluginJars.size());
            for (PluginJar pluginJar : pluginJars) {
                String key = generateKey(pluginJar.getUrl());
                componentMap.put(key, new JarPluginComponent(pluginJar));
            }
        }

        private String generateKey(URL url) {
            return url.toExternalForm();
        }

        public void addProfilerPlugin(ProfilerPlugin profilerPlugin) {
            URL profilerPluginUrl = CodeSourceUtils.getCodeLocation(profilerPlugin.getClass());
            if (profilerPluginUrl == null) {
                logger.warn("Unable to determine url for: {}", profilerPlugin.getClass());
                return;
            }
            String key = generateKey(profilerPluginUrl);
            JarPluginComponent jarPluginComponent = componentMap.get(key);
            if (jarPluginComponent == null) {
                logger.warn("Unexpected ProfilerPlugin: {}", profilerPlugin.getClass());
                return;
            }
            jarPluginComponent.addProfilerPlugin(profilerPlugin);
        }

        public Collection<JarPlugin<ProfilerPlugin>> buildJarPlugins() {
            List<JarPlugin<ProfilerPlugin>> jarPlugins = new ArrayList<JarPlugin<ProfilerPlugin>>(componentMap.size());
            for (JarPluginComponent component : componentMap.values()) {
                jarPlugins.add(component.toJarPlugin());
            }
            return jarPlugins;
        }

        private static class JarPluginComponent {
            private final PluginJar pluginJar;
            private final List<ProfilerPlugin> profilerPlugins;

            private JarPluginComponent(PluginJar pluginJar) {
                this.pluginJar = Assert.requireNonNull(pluginJar, "pluginJar");
                this.profilerPlugins = new ArrayList<ProfilerPlugin>();
            }

            private void addProfilerPlugin(ProfilerPlugin profilerPlugin) {
                if (profilerPlugin != null) {
                    profilerPlugins.add(profilerPlugin);
                }
            }

            private JarPlugin<ProfilerPlugin> toJarPlugin() {
                return new JarPlugin<ProfilerPlugin>(pluginJar, profilerPlugins, pluginJar.getPluginPackages());
            }
        }
    }
}
