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
import com.navercorp.pinpoint.common.service.DefaultTraceMetadataLoaderService;
import com.navercorp.pinpoint.common.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyProvider;
import com.navercorp.pinpoint.common.trace.ServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import com.navercorp.pinpoint.plugins.loader.trace.TraceMetadataProviderLoader;
import com.navercorp.pinpoint.profiler.context.module.PluginClassLoader;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceMetadataLoaderServiceProvider implements Provider<TraceMetadataLoaderService> {

    private final CommonLoggerFactory commonLoggerFactory;
    private final ClassLoader pluginClassLoader;

    @Inject
    public TraceMetadataLoaderServiceProvider(CommonLoggerFactory commonLoggerFactory, @PluginClassLoader ClassLoader pluginClassLoader) {
        this.commonLoggerFactory = Assert.requireNonNull(commonLoggerFactory, "commonLogger must not be null");
        this.pluginClassLoader = Assert.requireNonNull(pluginClassLoader, "pluginClassLoader must not be null");
    }

    @Override
    public TraceMetadataLoaderService get() {
        TraceMetadataProviderLoader traceMetadataProviderLoader = new TraceMetadataProviderLoader();
        List<TraceMetadataProvider> providers = traceMetadataProviderLoader.load(pluginClassLoader);

        TraceMetadataLoaderService typeLoaderService = new DefaultTraceMetadataLoaderService(providers, commonLoggerFactory);
        for (ServiceTypeInfo serviceTypeInfo : typeLoaderService.getServiceTypeInfos()) {
            ServiceTypeProvider.register(serviceTypeInfo.getServiceType());
        }
        for (AnnotationKey annotationKey : typeLoaderService.getAnnotationKeys()) {
            AnnotationKeyProvider.register(annotationKey);
        }
        return typeLoaderService;
    }

}
