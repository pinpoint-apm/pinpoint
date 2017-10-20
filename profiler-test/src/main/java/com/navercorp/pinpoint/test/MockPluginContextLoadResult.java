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
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.plugin.SetupResult;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockPluginContextLoadResult implements PluginContextLoadResult {
    private final ProfilerConfig profilerConfig;
    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;


    private List<SetupResult> lazy;

    public MockPluginContextLoadResult(ProfilerConfig profilerConfig, InstrumentEngine instrumentEngine, DynamicTransformTrigger dynamicTransformTrigger) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        if (dynamicTransformTrigger == null) {
            throw new NullPointerException("dynamicTransformTrigger must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.instrumentEngine = instrumentEngine;
        this.dynamicTransformTrigger = dynamicTransformTrigger;
    }

    private List<SetupResult> getProfilerPluginContextList() {
        if (lazy == null) {
            lazy = load();
        }
        return lazy;
    }


    private List<SetupResult> load() {

        List<ProfilerPlugin> plugins = PluginLoader.load(ProfilerPlugin.class, ClassLoader.getSystemClassLoader());

        List<SetupResult> pluginContexts = new ArrayList<SetupResult>();
        ClassInjector classInjector = new TestProfilerPluginClassLoader();
        PluginSetup pluginSetup = new MockPluginSetup(profilerConfig, instrumentEngine, dynamicTransformTrigger);
        for (ProfilerPlugin plugin : plugins) {
            SetupResult context = pluginSetup.setupPlugin(plugin, classInjector);
            pluginContexts.add(context);
        }
        return pluginContexts;
    }


    @Override
    public List<ClassFileTransformer> getClassFileTransformer() {
        List<ClassFileTransformer> classFileTransformerList = new ArrayList<ClassFileTransformer>();
        for (SetupResult pluginContext : getProfilerPluginContextList()) {
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
    public List<JdbcUrlParserV2> getJdbcUrlParserList() {
        return Collections.emptyList();
    }

}
