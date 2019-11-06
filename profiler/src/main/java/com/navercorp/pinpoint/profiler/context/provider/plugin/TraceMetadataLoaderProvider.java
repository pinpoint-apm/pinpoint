/*
 * Copyright 2019 NAVER Corp.
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
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataLoader;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import com.navercorp.pinpoint.loader.plugins.trace.TraceMetadataProviderLoader;
import com.navercorp.pinpoint.profiler.context.module.PluginClassLoader;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class TraceMetadataLoaderProvider implements Provider<TraceMetadataLoader> {

    private final CommonLoggerFactory commonLoggerFactory;
    private final ClassLoader pluginClassLoader;

    @Inject
    public TraceMetadataLoaderProvider(CommonLoggerFactory commonLoggerFactory, @PluginClassLoader ClassLoader pluginClassLoader) {
        this.commonLoggerFactory = Assert.requireNonNull(commonLoggerFactory, "commonLogger");
        this.pluginClassLoader = Assert.requireNonNull(pluginClassLoader, "pluginClassLoader");
    }

    @Override
    public TraceMetadataLoader get() {
        TraceMetadataProviderLoader traceMetadataProviderLoader = new TraceMetadataProviderLoader();
        List<TraceMetadataProvider> traceMetadataProviders = traceMetadataProviderLoader.load(pluginClassLoader);
        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(commonLoggerFactory);
        traceMetadataLoader.load(traceMetadataProviders);
        return traceMetadataLoader;
    }
}
