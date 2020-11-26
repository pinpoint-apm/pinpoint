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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractorProviderLocator;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatRecorderFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorRegistryService;
import com.navercorp.pinpoint.profiler.context.monitor.metric.CustomMetricRegistryService;
import com.navercorp.pinpoint.profiler.interceptor.factory.ExceptionHandlerFactory;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ObjectBinderFactoryProvider implements Provider<ObjectBinderFactory> {

    private final ProfilerConfig profilerConfig;
    private final Provider<TraceContext> traceContextProvider;
    private final DataSourceMonitorRegistryService dataSourceMonitorRegistryService;
    private final CustomMetricRegistryService customMetricRegistryService;
    private final Provider<ApiMetaDataService> apiMetaDataServiceProvider;
    private final ExceptionHandlerFactory exceptionHandlerFactory;
    private final RequestRecorderFactory requestRecorderFactory;
    private final Provider<UriStatRecorderFactory> uriStatRecorderFactoryProvider;


    @Inject
    public ObjectBinderFactoryProvider(ProfilerConfig profilerConfig,
                                       Provider<TraceContext> traceContextProvider,
                                       CustomMetricRegistryService customMetricRegistryService,
                                       DataSourceMonitorRegistryService dataSourceMonitorRegistryService,
                                       Provider<ApiMetaDataService> apiMetaDataServiceProvider,
                                       ExceptionHandlerFactory exceptionHandlerFactory,
                                       RequestRecorderFactory requestRecorderFactory,
                                       Provider<UriStatRecorderFactory> uriStatRecorderFactoryProvider) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.traceContextProvider = Assert.requireNonNull(traceContextProvider, "traceContextProvider");
        this.dataSourceMonitorRegistryService = Assert.requireNonNull(dataSourceMonitorRegistryService, "dataSourceMonitorRegistryService");
        this.customMetricRegistryService = Assert.requireNonNull(customMetricRegistryService, "customMetricRegistryService");
        this.apiMetaDataServiceProvider = Assert.requireNonNull(apiMetaDataServiceProvider, "apiMetaDataServiceProvider");
        this.exceptionHandlerFactory = Assert.requireNonNull(exceptionHandlerFactory, "exceptionHandlerFactory");
        this.requestRecorderFactory = Assert.requireNonNull(requestRecorderFactory, "requestRecorderFactory");
        this.uriStatRecorderFactoryProvider = Assert.requireNonNull(uriStatRecorderFactoryProvider, "uriStatRecorderFactoryProvider");
    }

    @Override
    public ObjectBinderFactory get() {
        return new ObjectBinderFactory(profilerConfig, traceContextProvider, dataSourceMonitorRegistryService, customMetricRegistryService, apiMetaDataServiceProvider, exceptionHandlerFactory, requestRecorderFactory, uriStatRecorderFactoryProvider);
    }

}
