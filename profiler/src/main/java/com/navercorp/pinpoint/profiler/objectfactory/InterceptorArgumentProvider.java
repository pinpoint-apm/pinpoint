/*
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.objectfactory;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Name;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.NoCache;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.CustomMetricRegistry;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatRecorderFactory;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.util.TypeUtils;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * @author Jongho Moon
 */
public class InterceptorArgumentProvider implements ArgumentProvider {
    private final DataSourceMonitorRegistry dataSourceMonitorRegistry;
    private final CustomMetricRegistry customMetricRegistry;
    private final ApiMetaDataService apiMetaDataService;
    private final InterceptorScope interceptorScope;
    private final InstrumentClass targetClass;
    private final InstrumentMethod targetMethod;
    private final RequestRecorderFactory requestRecorderFactory;
    private final UriStatRecorderFactory uriStatRecorderFactory;

    public InterceptorArgumentProvider(DataSourceMonitorRegistry dataSourceMonitorRegistry,
                                       CustomMetricRegistry customMetricRegistry,
                                       ApiMetaDataService apiMetaDataService,
                                       RequestRecorderFactory requestRecorderFactory,
                                       UriStatRecorderFactory uriStatRecorderFactory,
                                       InstrumentClass targetClass) {
        this(dataSourceMonitorRegistry, customMetricRegistry, apiMetaDataService, requestRecorderFactory, uriStatRecorderFactory, null, targetClass, null);
    }

    public InterceptorArgumentProvider(DataSourceMonitorRegistry dataSourceMonitorRegistry,
                                       CustomMetricRegistry customMetricRegistry,
                                       ApiMetaDataService apiMetaDataService,
                                       RequestRecorderFactory requestRecorderFactory,
                                       UriStatRecorderFactory uriStatRecorderFactory,
                                       InterceptorScope interceptorScope, InstrumentClass targetClass,
                                       InstrumentMethod targetMethod) {
        this.dataSourceMonitorRegistry = Objects.requireNonNull(dataSourceMonitorRegistry, "dataSourceMonitorRegistry");
        this.customMetricRegistry = Objects.requireNonNull(customMetricRegistry, "customMetricRegistry");
        this.apiMetaDataService = Objects.requireNonNull(apiMetaDataService, "apiMetaDataService");

        this.requestRecorderFactory = requestRecorderFactory;
        this.uriStatRecorderFactory = uriStatRecorderFactory;
        this.interceptorScope = interceptorScope;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;

    }

    @Override
    public Option get(int index, Class<?> type, Annotation[] annotations) {
        if (type == InstrumentClass.class) {
            return Option.withValue(targetClass);
        } else if (type == MethodDescriptor.class) {
            MethodDescriptor descriptor = targetMethod.getDescriptor();
            cacheApiIfAnnotationNotPresent(annotations, descriptor);

            return Option.withValue(descriptor);
        } else if (type == InstrumentMethod.class) {
            return Option.withValue(targetMethod);
        } else if (type == InterceptorScope.class) {
            Name annotation = TypeUtils.findAnnotation(annotations, Name.class);

            if (annotation == null) {
                if (interceptorScope == null) {
                    throw new PinpointException("Scope parameter is not annotated with @Name and the target class is not associated with any Scope");
                } else {
                    return Option.withValue(interceptorScope);
                }
            } else {
                return Option.empty();
            }
        } else if (type == DataSourceMonitorRegistry.class) {
            return Option.withValue(dataSourceMonitorRegistry);
        } else if (type == RequestRecorderFactory.class) {
            return Option.withValue(requestRecorderFactory);
        } else if (type == CustomMetricRegistry.class) {
            return Option.withValue(customMetricRegistry);
        } else if (type == UriStatRecorderFactory.class) {
            return Option.withValue(uriStatRecorderFactory);
        }

        return Option.empty();
    }

    private void cacheApiIfAnnotationNotPresent(Annotation[] annotations, MethodDescriptor descriptor) {
        Annotation annotation = TypeUtils.findAnnotation(annotations, NoCache.class);
        if (annotation == null) {
            this.apiMetaDataService.cacheApi(descriptor);
        }
    }
}
