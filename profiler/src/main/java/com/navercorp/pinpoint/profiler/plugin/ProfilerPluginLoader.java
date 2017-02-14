/*
 * Copyright 2014 NAVER Corp.
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

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClassPool;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.profiler.instrument.ClassInjector;
import com.navercorp.pinpoint.profiler.instrument.JarProfilerPluginClassInjector;

/**
 * @author Jongho Moon
 *
 */
public class ProfilerPluginLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ApplicationContext applicationContext;
    private final ClassNameFilter profilerPackageFilter = new PinpointProfilerPackageSkipFilter();

    private final PluginSetup pluginSetup;

    public ProfilerPluginLoader(ApplicationContext applicationContext, PluginSetup pluginSetup) {
        if (applicationContext == null) {
            throw new NullPointerException("applicationContext must not be null");
        }
        this.applicationContext = applicationContext;
        this.pluginSetup = pluginSetup;
    }

    public List<DefaultProfilerPluginContext> load(URL[] pluginJars) {
        List<DefaultProfilerPluginContext> pluginContexts = new ArrayList<DefaultProfilerPluginContext>(pluginJars.length);
        List<String> disabled = applicationContext.getProfilerConfig().getDisabledPlugins();
        
        for (URL jar : pluginJars) {

            final JarFile pluginJarFile = createJarFile(jar);
            final List<String> pluginPackageList = getPluginPackage(pluginJarFile);

            final ClassNameFilter pluginFilterChain = createPluginFilterChain(pluginPackageList);

            final List<ProfilerPlugin> plugins = PluginLoader.load(ProfilerPlugin.class, new URL[] { jar });

            for (ProfilerPlugin plugin : plugins) {
                if (disabled.contains(plugin.getClass().getName())) {
                    logger.info("Skip disabled plugin: {}", plugin.getClass().getName());
                    continue;
                }
                if (logger.isInfoEnabled()) {
                    logger.info("{} Plugin {}:{}", plugin.getClass(), PluginConfig.PINPOINT_PLUGIN_PACKAGE, pluginPackageList);
                }
                
                logger.info("Loading plugin:{} pluginPackage:{}", plugin.getClass().getName(), plugin);

                PluginConfig pluginConfig = new PluginConfig(jar, plugin, applicationContext.getInstrumentation(), applicationContext.getClassPool(), applicationContext.getBootstrapJarPaths(), pluginFilterChain);
                final ClassInjector classInjector = new JarProfilerPluginClassInjector(pluginConfig);
                final DefaultProfilerPluginContext context = pluginSetup.setupPlugin(plugin, classInjector);
                pluginContexts.add(context);
            }
        }
        

        return pluginContexts;
    }

    private ClassNameFilter createPluginFilterChain(List<String> packageList) {

        final ClassNameFilter pluginPackageFilter = new PluginPackageFilter(packageList);

        final List<ClassNameFilter> chain = Arrays.asList(profilerPackageFilter, pluginPackageFilter);

        final ClassNameFilter filterChain = new ClassNameFilterChain(chain);

        return filterChain;
    }

    private JarFile createJarFile(URL pluginJar) {
        try {
            final URI uri = pluginJar.toURI();
            return new JarFile(new File(uri));
        } catch (URISyntaxException e) {
            throw new RuntimeException("URISyntax error. " + e.getCause(), e);
        } catch (IOException e) {
            throw new RuntimeException("IO error. " + e.getCause(), e);
        }
    }
    private Manifest getManifest(JarFile pluginJarFile) {
        try {
            return pluginJarFile.getManifest();
        } catch (IOException ex) {
            logger.info("{} IoError :{}", pluginJarFile.getName(), ex.getMessage(), ex);
            return null;
        }
    }

    public List<String> getPluginPackage(JarFile pluginJarFile) {

        final Manifest manifest =  getManifest(pluginJarFile);
        if (manifest == null) {
            return PluginConfig.DEFAULT_PINPOINT_PLUGIN_PACKAGE_NAME;
        }

        final Attributes attributes = manifest.getMainAttributes();
        final String pluginPackage = attributes.getValue(PluginConfig.PINPOINT_PLUGIN_PACKAGE);
        if (pluginPackage == null) {
            return PluginConfig.DEFAULT_PINPOINT_PLUGIN_PACKAGE_NAME;
        }
        return StringUtils.splitAndTrim(pluginPackage, ",");
    }


}
