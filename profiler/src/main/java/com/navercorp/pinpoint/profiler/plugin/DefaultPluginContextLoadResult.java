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

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentEngine;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.profiler.context.ApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.BootstrapJarPaths;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultPluginContextLoadResult implements PluginContextLoadResult {

    private final URL[] pluginJars;
    private final Instrumentation instrumentation;
    private final InstrumentEngine instrumentEngine;
    private final List<String> bootstrapJarPaths;
    private final ProfilerConfig profilerConfig;
    private final ApplicationContext applicationContext;
    private final DynamicTransformTrigger dynamicTransformTrigger;

    private List<SetupResult> lazy;

    public DefaultPluginContextLoadResult(ProfilerConfig profilerConfig, ApplicationContext applicationContext, DynamicTransformTrigger dynamicTransformTrigger, Instrumentation instrumentation, InstrumentEngine instrumentEngine,
                                          @BootstrapJarPaths List<String> bootstrapJarPaths, URL[] pluginJars) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (applicationContext == null) {
            throw new NullPointerException("applicationContext must not be null");
        }
        if (dynamicTransformTrigger == null) {
            throw new NullPointerException("dynamicTransformTrigger must not be null");
        }
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        if (bootstrapJarPaths == null) {
            throw new NullPointerException("bootstrapJarPaths must not be null");
        }
        if (pluginJars == null) {
            throw new NullPointerException("pluginJars must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.applicationContext = applicationContext;
        this.dynamicTransformTrigger = dynamicTransformTrigger;

        this.pluginJars = pluginJars;
        this.instrumentation = instrumentation;
        this.instrumentEngine = instrumentEngine;
        this.bootstrapJarPaths = bootstrapJarPaths;
    }


    private List<SetupResult> getProfilerPluginContextList() {
        if (lazy == null) {
            lazy = load();
        }
        return lazy;
    }


    private List<SetupResult> load() {
        PluginSetup pluginSetup = new DefaultPluginSetup(profilerConfig, applicationContext, dynamicTransformTrigger);
        final ProfilerPluginLoader loader = new ProfilerPluginLoader(profilerConfig, pluginSetup, instrumentation, instrumentEngine, bootstrapJarPaths);
        List<SetupResult> load = loader.load(pluginJars);
        return load;
    }

    @Override
    public List<ClassFileTransformer> getClassFileTransformer() {
        // TODO Need plugin context level grouping
        List<SetupResult> profilerPluginContextList = getProfilerPluginContextList();
        List<ClassFileTransformer> transformerList = new ArrayList<ClassFileTransformer>();
        for (SetupResult pluginContext : profilerPluginContextList) {
            List<ClassFileTransformer> classTransformerList = pluginContext.getClassTransformerList();
            transformerList.addAll(classTransformerList);
        }
        return transformerList;
    }



    @Override
    public List<ApplicationTypeDetector> getApplicationTypeDetectorList() {

        List<ApplicationTypeDetector> registeredDetectors = new ArrayList<ApplicationTypeDetector>();

        List<SetupResult> profilerPluginContextList = getProfilerPluginContextList();
        for (SetupResult context : profilerPluginContextList) {
            List<ApplicationTypeDetector> applicationTypeDetectors = context.getApplicationTypeDetectors();
            registeredDetectors.addAll(applicationTypeDetectors);
        }

        return registeredDetectors;
    }

    @Override
    public List<JdbcUrlParserV2> getJdbcUrlParserList() {
        List<JdbcUrlParserV2> result = new ArrayList<JdbcUrlParserV2>();

        List<SetupResult> profilerPluginContextList = getProfilerPluginContextList();
        for (SetupResult context : profilerPluginContextList) {
            List<JdbcUrlParserV2> jdbcUrlParserList = context.getJdbcUrlParserList();
            result.addAll(jdbcUrlParserList);
        }

        return result;
    }

}
