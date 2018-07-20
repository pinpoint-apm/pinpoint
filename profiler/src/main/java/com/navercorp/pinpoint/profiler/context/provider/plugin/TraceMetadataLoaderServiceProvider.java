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
import com.navercorp.pinpoint.common.plugin.Plugin;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.service.DefaultTraceMetadataLoaderService;
import com.navercorp.pinpoint.common.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceMetadataLoaderServiceProvider implements Provider<TraceMetadataLoaderService> {

    private final PluginLoader pluginLoader;
    private final CommonLoggerFactory commonLoggerFactory;

    @Inject
    public TraceMetadataLoaderServiceProvider(CommonLoggerFactory commonLoggerFactory, PluginLoader pluginLoader) {
        this.commonLoggerFactory = Assert.requireNonNull(commonLoggerFactory, "commonLogger must not be null");
        this.pluginLoader = Assert.requireNonNull(pluginLoader, "pluginLoader must not be null");
    }

    @Override
    public TraceMetadataLoaderService get() {

        List<Plugin<TraceMetadataProvider>> plugins = pluginLoader.load(TraceMetadataProvider.class);
        List<TraceMetadataProvider> providers = new ArrayList<TraceMetadataProvider>();
        for (Plugin<TraceMetadataProvider> plugin : plugins) {
            List<TraceMetadataProvider> pluginList = plugin.getInstanceList();
            providers.addAll(pluginList);
        }

        TraceMetadataLoaderService typeLoaderService = new DefaultTraceMetadataLoaderService(providers, commonLoggerFactory);
        return typeLoaderService;
    }

}
