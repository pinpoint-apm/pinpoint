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

package com.navercorp.pinpoint.profiler.objectfactory;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.CustomMetricRegistry;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatRecorderFactory;
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorRegistryAdaptor;
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorRegistryService;
import com.navercorp.pinpoint.profiler.context.monitor.metric.CustomMetricRegistryAdaptor;
import com.navercorp.pinpoint.profiler.context.monitor.metric.CustomMetricRegistryService;
import com.navercorp.pinpoint.profiler.interceptor.factory.AnnotatedInterceptorFactory;
import com.navercorp.pinpoint.profiler.interceptor.factory.ExceptionHandlerFactory;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;

import com.google.inject.Provider;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ObjectBinderFactory {
    private final ProfilerConfig profilerConfig;
    private final Provider<TraceContext> traceContextProvider;
    private final DataSourceMonitorRegistry dataSourceMonitorRegistry;
    private final CustomMetricRegistry customMetricRegistry;
    private final Provider<ApiMetaDataService> apiMetaDataServiceProvider;

    private final ExceptionHandlerFactory exceptionHandlerFactory;
    private final RequestRecorderFactory requestRecorderFactory;
    private final Provider<UriStatRecorderFactory> uriStatRecorderFactoryProvider;


    public ObjectBinderFactory(ProfilerConfig profilerConfig,
                               Provider<TraceContext> traceContextProvider,
                               DataSourceMonitorRegistryService dataSourceMonitorRegistryService,
                               CustomMetricRegistryService customMonitorRegistryService,
                               Provider<ApiMetaDataService> apiMetaDataServiceProvider,
                               ExceptionHandlerFactory exceptionHandlerFactory,
                               RequestRecorderFactory requestRecorderFactory,
                               Provider<UriStatRecorderFactory> uriStatRecorderFactoryProvider) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.traceContextProvider = Objects.requireNonNull(traceContextProvider, "traceContextProvider");

        Objects.requireNonNull(dataSourceMonitorRegistryService, "dataSourceMonitorRegistryService");
        this.dataSourceMonitorRegistry = new DataSourceMonitorRegistryAdaptor(dataSourceMonitorRegistryService);

        Objects.requireNonNull(customMonitorRegistryService, "customMonitorRegistryService");
        this.customMetricRegistry = new CustomMetricRegistryAdaptor(customMonitorRegistryService);

        this.apiMetaDataServiceProvider = Objects.requireNonNull(apiMetaDataServiceProvider, "apiMetaDataServiceProvider");

        this.exceptionHandlerFactory = Objects.requireNonNull(exceptionHandlerFactory, "exceptionHandlerFactory");
        this.requestRecorderFactory = Objects.requireNonNull(requestRecorderFactory, "requestRecorderFactory");

        this.uriStatRecorderFactoryProvider = Objects.requireNonNull(uriStatRecorderFactoryProvider, "uriStatRecorderFactoryProvider");
    }

    public AutoBindingObjectFactory newAutoBindingObjectFactory(InstrumentContext pluginContext, ClassLoader classLoader, ArgumentProvider... argumentProviders) {
        final TraceContext traceContext = this.traceContextProvider.get();
        return new AutoBindingObjectFactory(profilerConfig, traceContext, pluginContext, classLoader, argumentProviders);
    }


    public InterceptorArgumentProvider newInterceptorArgumentProvider(InstrumentClass instrumentClass) {
        ApiMetaDataService apiMetaDataService = this.apiMetaDataServiceProvider.get();

        UriStatRecorderFactory uriStatRecorderFactory = uriStatRecorderFactoryProvider.get();
        return new InterceptorArgumentProvider(dataSourceMonitorRegistry, customMetricRegistry, apiMetaDataService, requestRecorderFactory, uriStatRecorderFactory, instrumentClass);
    }

    public AnnotatedInterceptorFactory newAnnotatedInterceptorFactory(InstrumentContext pluginContext) {
        final TraceContext traceContext = this.traceContextProvider.get();
        ApiMetaDataService apiMetaDataService = this.apiMetaDataServiceProvider.get();

        UriStatRecorderFactory uriStatRecorderFactory = uriStatRecorderFactoryProvider.get();
        return new AnnotatedInterceptorFactory(profilerConfig, traceContext, dataSourceMonitorRegistry, customMetricRegistry, apiMetaDataService,
                pluginContext, exceptionHandlerFactory, requestRecorderFactory, uriStatRecorderFactory);
    }
}
