/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.plugin;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.plugin.JarPluginLoader;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.service.DefaultTraceMetadataLoaderService;
import com.navercorp.pinpoint.common.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.PluginJars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceMetadataLoaderServiceProvider implements Provider<TraceMetadataLoaderService> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final URL[] pluginJars;

    private final PluginLoader pluginLoader;
    @Inject
    public TraceMetadataLoaderServiceProvider(@PluginJars URL[] pluginJars, PluginLoader pluginLoader) {
        this.pluginJars = Assert.requireNonNull(pluginJars, "pluginJars must not be null");
        this.pluginLoader = Assert.requireNonNull(pluginLoader, "pluginLoader must not be null");
    }

    @Override
    public TraceMetadataLoaderService get() {
        Slf4jCommonLoggerFactory slf4jCommonLoggerFactory = new Slf4jCommonLoggerFactory();

        List<TraceMetadataProvider> providers = new ArrayList<TraceMetadataProvider>();
        for (URL pluginJar : pluginJars) {
            List<TraceMetadataProvider> load = pluginLoader.load(pluginJar, TraceMetadataProvider.class);
            providers.addAll(load);
        }

        TraceMetadataLoaderService typeLoaderService = new DefaultTraceMetadataLoaderService(providers, slf4jCommonLoggerFactory);
        return typeLoaderService;
    }

}
