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

import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.profiler.instrument.ClassInjector;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockPluginContextLoadResult implements PluginContextLoadResult {
    private final Provider<PluginSetup> provider;

    public MockPluginContextLoadResult(Provider<PluginSetup> provider ) {
        this.provider = provider;
    }

    @Override
    public List<DefaultProfilerPluginContext> getProfilerPluginContextList() {
        PluginSetup pluginSetup = provider.get();
        List<DefaultProfilerPluginContext> pluginContexts = new ArrayList<DefaultProfilerPluginContext>();
        ClassInjector classInjector = new TestProfilerPluginClassLoader();

        List<ProfilerPlugin> plugins = PluginLoader.load(ProfilerPlugin.class, ClassLoader.getSystemClassLoader());

        for (ProfilerPlugin plugin : plugins) {
            DefaultProfilerPluginContext context = pluginSetup.setupPlugin(plugin, classInjector);
            pluginContexts.add(context);
        }
        return pluginContexts;
    }
}
