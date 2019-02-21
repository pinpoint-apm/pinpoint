/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginGlobalContext;
import com.navercorp.pinpoint.common.plugin.Plugin;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginGlobalContext;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.plugin.PluginSetupResult;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockPluginContextLoadResult implements PluginContextLoadResult {
    private final ProfilerConfig profilerConfig;
    private final ServiceType configuredApplicationType;
    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final PluginLoader pluginLoader;

    private List<PluginSetupResult> lazy;

    public MockPluginContextLoadResult(ProfilerConfig profilerConfig, ServiceType configuredApplicationType,
                                       InstrumentEngine instrumentEngine, DynamicTransformTrigger dynamicTransformTrigger, PluginLoader pluginLoader) {

        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.configuredApplicationType = Assert.requireNonNull(configuredApplicationType, "configuredApplicationType must not be null");
        this.instrumentEngine = Assert.requireNonNull(instrumentEngine, "instrumentEngine must not be null");
        this.dynamicTransformTrigger = Assert.requireNonNull(dynamicTransformTrigger, "dynamicTransformTrigger must not be null");
        this.pluginLoader = Assert.requireNonNull(pluginLoader, "pluginLoader must not be null");
    }

    private List<PluginSetupResult> getProfilerPluginContextList() {
        if (lazy == null) {
            lazy = load();
        }
        return lazy;
    }

    private List<PluginSetupResult> load() {
        List<Plugin<ProfilerPlugin>> plugins = pluginLoader.load(ProfilerPlugin.class);

        List<PluginSetupResult> pluginContexts = new ArrayList<PluginSetupResult>();
        ClassInjector classInjector = new TestProfilerPluginClassLoader();
        ProfilerPluginGlobalContext globalContext = new DefaultProfilerPluginGlobalContext(profilerConfig, configuredApplicationType);
        PluginSetup pluginSetup = new MockPluginSetup(instrumentEngine, dynamicTransformTrigger);
        for (Plugin<ProfilerPlugin> plugin : plugins) {
            for (ProfilerPlugin profilerPlugin : plugin.getInstanceList()) {
                PluginSetupResult context = pluginSetup.setupPlugin(globalContext, profilerPlugin, classInjector);
                pluginContexts.add(context);
            }
        }
        return pluginContexts;
    }


    @Override
    public List<ClassFileTransformer> getClassFileTransformer() {
        List<ClassFileTransformer> classFileTransformerList = new ArrayList<ClassFileTransformer>();
        for (PluginSetupResult pluginContext : getProfilerPluginContextList()) {
            List<ClassFileTransformer> classFileTransformer = pluginContext.getClassTransformerList();
            classFileTransformerList.addAll(classFileTransformer);
        }

        return classFileTransformerList;
    }

    @Override
    public List<ApplicationTypeDetector> getApplicationTypeDetectorList() {
        return Collections.emptyList();
    }

    @Override
    public ServiceType getApplicationType() {
        return null;
    }

    @Override
    public List<JdbcUrlParserV2> getJdbcUrlParserList() {
        return Collections.emptyList();
    }

}
