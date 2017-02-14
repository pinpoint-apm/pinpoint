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

import com.navercorp.pinpoint.bootstrap.instrument.GuardInstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.profiler.context.ApplicationContext;
import com.navercorp.pinpoint.profiler.instrument.ClassInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultPluginSetup implements PluginSetup {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ApplicationContext applicationContext;


    public DefaultPluginSetup(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public DefaultProfilerPluginContext setupPlugin(ProfilerPlugin plugin, ClassInjector classInjector) {

        final DefaultProfilerPluginContext context = new DefaultProfilerPluginContext(applicationContext, classInjector);
        final GuardProfilerPluginContext guard = new GuardProfilerPluginContext(context);
        final GuardInstrumentContext guardInstrumentContext = preparePlugin(plugin, context);
        try {
            // WARN external plugin api
            if (logger.isInfoEnabled()) {
                logger.info("{} Plugin setup", plugin.getClass().getName());
            }
            plugin.setup(guard);
        } finally {
            guard.close();
            guardInstrumentContext.close();
        }
        return context;
    }

    private GuardInstrumentContext preparePlugin(ProfilerPlugin plugin, InstrumentContext context) {

        final GuardInstrumentContext guardInstrumentContext = new GuardInstrumentContext(context);
        if (plugin instanceof TransformTemplateAware) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}.setTransformTemplate", plugin.getClass().getName());
            }
            final TransformTemplate transformTemplate = new TransformTemplate(guardInstrumentContext);
            ((TransformTemplateAware) plugin).setTransformTemplate(transformTemplate);
        }
        return guardInstrumentContext;
    }

}
