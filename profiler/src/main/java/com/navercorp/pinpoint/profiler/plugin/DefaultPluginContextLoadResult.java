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
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.provider.plugin.ProfilerPluginLoader;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultPluginContextLoadResult implements PluginContextLoadResult {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InstrumentEngine instrumentEngine;
    private final ClassInjectorFactory classInjectorFactory;

    private final ProfilerConfig profilerConfig;
    private final DynamicTransformTrigger dynamicTransformTrigger;

    private final List<SetupResult> setupResultList;

    public DefaultPluginContextLoadResult(ProfilerConfig profilerConfig, DynamicTransformTrigger dynamicTransformTrigger, InstrumentEngine instrumentEngine,
                                          PluginLoader pluginLoader, ClassInjectorFactory classInjectorFactory) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.dynamicTransformTrigger = Assert.requireNonNull(dynamicTransformTrigger, "dynamicTransformTrigger must not be null");
        this.instrumentEngine = Assert.requireNonNull(instrumentEngine, "instrumentEngine must not be null");


        this.classInjectorFactory = Assert.requireNonNull(classInjectorFactory, "bootstrapCore must not be null");

        this.setupResultList = load(pluginLoader);
    }




    private List<SetupResult> load(PluginLoader pluginLoader) {
        Assert.requireNonNull(pluginLoader, "pluginLoader must not be null");

        logger.info("load plugin");
        PluginSetup pluginSetup = new DefaultPluginSetup(profilerConfig, instrumentEngine, dynamicTransformTrigger);
        final ProfilerPluginLoader loader = new ProfilerPluginLoader(profilerConfig, pluginSetup, classInjectorFactory, pluginLoader);
        List<SetupResult> load = loader.load();
        return load;
    }

    @Override
    public List<ClassFileTransformer> getClassFileTransformer() {
        // TODO Need plugin context level grouping
        final List<ClassFileTransformer> transformerList = new ArrayList<ClassFileTransformer>();
        for (SetupResult pluginContext : setupResultList) {
            List<ClassFileTransformer> classTransformerList = pluginContext.getClassTransformerList();
            transformerList.addAll(classTransformerList);
        }
        return transformerList;
    }



    @Override
    public List<ApplicationTypeDetector> getApplicationTypeDetectorList() {

        final List<ApplicationTypeDetector> registeredDetectors = new ArrayList<ApplicationTypeDetector>();

        for (SetupResult context : setupResultList) {
            List<ApplicationTypeDetector> applicationTypeDetectors = context.getApplicationTypeDetectors();
            registeredDetectors.addAll(applicationTypeDetectors);
        }

        return registeredDetectors;
    }

    @Override
    public List<JdbcUrlParserV2> getJdbcUrlParserList() {
        final List<JdbcUrlParserV2> result = new ArrayList<JdbcUrlParserV2>();

        for (SetupResult context : setupResultList) {
            List<JdbcUrlParserV2> jdbcUrlParserList = context.getJdbcUrlParserList();
            result.addAll(jdbcUrlParserList);
        }

        return result;
    }

}
