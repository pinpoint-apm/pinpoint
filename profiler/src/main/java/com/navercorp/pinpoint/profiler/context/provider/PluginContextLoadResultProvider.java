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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClassPool;
import com.navercorp.pinpoint.profiler.context.ApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.BootstrapJarPaths;
import com.navercorp.pinpoint.profiler.context.module.PluginJars;
import com.navercorp.pinpoint.profiler.plugin.DefaultPluginContextLoadResult;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.plugin.ProfilerPluginLoader;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginContextLoadResultProvider implements Provider<PluginContextLoadResult> {


    private final URL[] pluginJars;
    private final PluginSetup pluginSetup;
    private final ApplicationContext applicationContext;

    @Inject
    public PluginContextLoadResultProvider(ApplicationContext applicationContext, @PluginJars URL[] pluginJars, PluginSetup pluginSetup) {
        if (applicationContext == null) {
            throw new NullPointerException("applicationContext must not be null");
        }
        if (pluginJars == null) {
            throw new NullPointerException("pluginJars must not be null");
        }
        if (pluginSetup == null) {
            throw new NullPointerException("pluginSetup must not be null");
        }
        this.applicationContext = applicationContext;
        this.pluginJars = pluginJars;
        this.pluginSetup = pluginSetup;
    }

    @Override
    public PluginContextLoadResult get() {
        final ProfilerPluginLoader loader = new ProfilerPluginLoader(applicationContext, pluginSetup);
        List<DefaultProfilerPluginContext> load = loader.load(pluginJars);
        return new DefaultPluginContextLoadResult(load);

    }
}
