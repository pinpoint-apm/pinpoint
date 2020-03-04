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
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginGlobalContext;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.GuardInstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultPluginSetup implements PluginSetup {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;


    public DefaultPluginSetup(InstrumentEngine instrumentEngine, DynamicTransformTrigger dynamicTransformTrigger) {
        this.instrumentEngine = Assert.requireNonNull(instrumentEngine, "instrumentEngine");
        this.dynamicTransformTrigger = Assert.requireNonNull(dynamicTransformTrigger, "dynamicTransformTrigger");
    }

    @Override
    public PluginSetupResult setupPlugin(ProfilerPluginGlobalContext globalContext, ProfilerPlugin profilerPlugin, ClassInjector classInjector) {
        final ProfilerConfig profilerConfig = globalContext.getConfig();
        final ClassFileTransformerLoader transformerRegistry = new ClassFileTransformerLoader(profilerConfig, dynamicTransformTrigger);
        final DefaultProfilerPluginSetupContext setupContext = new DefaultProfilerPluginSetupContext(globalContext);
        final GuardProfilerPluginSetupContext guardSetupContext = new GuardProfilerPluginSetupContext(setupContext);

        final InstrumentContext instrumentContext = new PluginInstrumentContext(profilerConfig, instrumentEngine, dynamicTransformTrigger, classInjector, transformerRegistry);
        final GuardInstrumentContext guardInstrumentContext = preparePlugin(profilerPlugin, instrumentContext);
        try {
            // WARN external plugin api
            if (logger.isInfoEnabled()) {
                logger.info("{} Plugin setup", profilerPlugin.getClass().getName());
            }
            profilerPlugin.setup(guardSetupContext);
        } finally {
            guardSetupContext.close();
            guardInstrumentContext.close();
        }
        PluginSetupResult setupResult = new PluginSetupResult(setupContext, transformerRegistry);
        return setupResult;
    }

    private GuardInstrumentContext preparePlugin(ProfilerPlugin plugin, InstrumentContext instrumentContext) {

        final GuardInstrumentContext guardInstrumentContext = new GuardInstrumentContext(instrumentContext);
        if (plugin instanceof TransformTemplateAware) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}.setTransformTemplate", plugin.getClass().getName());
            }
            final TransformTemplate transformTemplate = new TransformTemplate(guardInstrumentContext);
            ((TransformTemplateAware) plugin).setTransformTemplate(transformTemplate);
        } else if(plugin instanceof MatchableTransformTemplateAware) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}.setTransformTemplate", plugin.getClass().getName());
            }
            final MatchableTransformTemplate transformTemplate = new MatchableTransformTemplate(guardInstrumentContext);
            ((MatchableTransformTemplateAware) plugin).setTransformTemplate(transformTemplate);
        }
        return guardInstrumentContext;
    }

}
