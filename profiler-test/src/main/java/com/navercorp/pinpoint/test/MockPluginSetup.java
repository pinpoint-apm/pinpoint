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

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginGlobalContext;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import com.navercorp.pinpoint.profiler.plugin.ClassFileTransformerLoader;
import com.navercorp.pinpoint.profiler.plugin.PluginInstrumentContext;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginSetupContext;
import com.navercorp.pinpoint.profiler.plugin.GuardProfilerPluginSetupContext;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.plugin.PluginSetupResult;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockPluginSetup implements PluginSetup {


    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;

    @Inject
    public MockPluginSetup(InstrumentEngine instrumentEngine, DynamicTransformTrigger dynamicTransformTrigger) {
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine");
        }
        if (dynamicTransformTrigger == null) {
            throw new NullPointerException("dynamicTransformTrigger");
        }
        this.instrumentEngine = instrumentEngine;
        this.dynamicTransformTrigger = dynamicTransformTrigger;
    }

    @Override
    public PluginSetupResult setupPlugin(ProfilerPluginGlobalContext globalContext, ProfilerPlugin plugin, ClassInjector classInjector) {
        final ProfilerConfig profilerConfig = globalContext.getConfig();
        final DefaultProfilerPluginSetupContext pluginSetupContext = new DefaultProfilerPluginSetupContext(globalContext);
        final GuardProfilerPluginSetupContext guardPluginSetupContext = new GuardProfilerPluginSetupContext(pluginSetupContext);

        ClassFileTransformerLoader classFileTransformerLoader = new ClassFileTransformerLoader(profilerConfig, dynamicTransformTrigger);
        InstrumentContext instrumentContext = new PluginInstrumentContext(profilerConfig, instrumentEngine, dynamicTransformTrigger, classInjector, classFileTransformerLoader);
        try {
            preparePlugin(plugin, instrumentContext);
            plugin.setup(guardPluginSetupContext);
        } finally {
            guardPluginSetupContext.close();
        }
        PluginSetupResult setup = new PluginSetupResult(pluginSetupContext, classFileTransformerLoader);
        return setup;
    }


    /**
     * TODO duplicated code : com/navercorp/pinpoint/profiler/plugin/ProfilerPluginLoader.java
     * @param plugin
     * @param context
     */
    private void preparePlugin(ProfilerPlugin plugin, InstrumentContext context) {

        if (plugin instanceof TransformTemplateAware) {
            final TransformTemplate transformTemplate = new TransformTemplate(context);
            ((TransformTemplateAware) plugin).setTransformTemplate(transformTemplate);
        } else if (plugin instanceof MatchableTransformTemplateAware) {
            final MatchableTransformTemplate transformTemplate = new MatchableTransformTemplate(context);
            ((MatchableTransformTemplateAware) plugin).setTransformTemplate(transformTemplate);
        }
    }
}
