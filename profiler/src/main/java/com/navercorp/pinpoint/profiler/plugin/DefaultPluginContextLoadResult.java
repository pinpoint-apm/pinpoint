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
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClassPool;
import com.navercorp.pinpoint.profiler.context.module.BootstrapJarPaths;

import javax.inject.Provider;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultPluginContextLoadResult implements PluginContextLoadResult {

    private final Provider<PluginSetup> pluginSetup;
    private final URL[] pluginJars;
    private final Instrumentation instrumentation;
    private final InstrumentClassPool instrumentClassPool;
    private final List<String> bootstrapJarPaths;
    private final ProfilerConfig profilerConfig;

    private List<DefaultProfilerPluginContext> lazy;

    public DefaultPluginContextLoadResult(ProfilerConfig profilerConfig, Instrumentation instrumentation, InstrumentClassPool instrumentClassPool, @BootstrapJarPaths List<String> bootstrapJarPaths, Provider<PluginSetup> pluginSetup, URL[] pluginJars) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (instrumentClassPool == null) {
            throw new NullPointerException("instrumentClassPool must not be null");
        }
        if (bootstrapJarPaths == null) {
            throw new NullPointerException("bootstrapJarPaths must not be null");
        }
        if (pluginSetup == null) {
            throw new NullPointerException("pluginSetup must not be null");
        }
        if (pluginJars == null) {
            throw new NullPointerException("pluginJars must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.pluginSetup = pluginSetup;
        this.pluginJars = pluginJars;
        this.instrumentation = instrumentation;
        this.instrumentClassPool = instrumentClassPool;
        this.bootstrapJarPaths = bootstrapJarPaths;
    }

    @Override
    public List<DefaultProfilerPluginContext> getProfilerPluginContextList() {
        if (lazy == null) {
            lazy = load();
        }
        return lazy;
    }

    private List<DefaultProfilerPluginContext> load() {
        PluginSetup pluginSetup = this.pluginSetup.get();
        final ProfilerPluginLoader loader = new ProfilerPluginLoader(profilerConfig, pluginSetup, instrumentation, instrumentClassPool, bootstrapJarPaths);
        List<DefaultProfilerPluginContext> load = loader.load(pluginJars);
        return load;
    }
}
