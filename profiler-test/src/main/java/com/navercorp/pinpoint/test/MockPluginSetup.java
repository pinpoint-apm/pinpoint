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
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.profiler.context.ApplicationContext;
import com.navercorp.pinpoint.profiler.instrument.ClassInjector;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.GuardProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockPluginSetup implements PluginSetup {


    private final ApplicationContext applicationContext;

    @Inject
    public MockPluginSetup(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public DefaultProfilerPluginContext setupPlugin(ProfilerPlugin plugin, ClassInjector classInjector) {
        final DefaultProfilerPluginContext context = new DefaultProfilerPluginContext(applicationContext, classInjector);

        final GuardProfilerPluginContext guard = new GuardProfilerPluginContext(context);
        try {
            preparePlugin(plugin, context);
            plugin.setup(guard);
        } finally {
            guard.close();
        }
        return context;
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
        }
    }
}
