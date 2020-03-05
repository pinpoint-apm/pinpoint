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
import com.navercorp.pinpoint.profiler.metadata.DefaultTraceMetadataLoaderService;
import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataRegistrar;
import com.navercorp.pinpoint.common.profiler.trace.AnnotationKeyMatcherRegistry;
import com.navercorp.pinpoint.common.profiler.trace.AnnotationKeyRegistry;
import com.navercorp.pinpoint.common.profiler.trace.ServiceTypeRegistry;
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataLoader;
import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 * @author HyunGil Jeong
 */
public class TraceMetadataLoaderServiceProvider implements Provider<TraceMetadataLoaderService> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServiceTypeRegistry serviceTypeRegistry;
    private final AnnotationKeyRegistry annotationKeyRegistry;
    private final AnnotationKeyMatcherRegistry annotationKeyMatcherRegistry;

    @Inject
    public TraceMetadataLoaderServiceProvider(TraceMetadataLoader traceMetadataLoader) {
        Assert.requireNonNull(traceMetadataLoader, "traceMetadataLoader");
        this.serviceTypeRegistry = traceMetadataLoader.createServiceTypeRegistry();
        this.annotationKeyRegistry = traceMetadataLoader.createAnnotationKeyRegistry();
        this.annotationKeyMatcherRegistry = traceMetadataLoader.createAnnotationKeyMatcherRegistry();
    }

    @Override
    public TraceMetadataLoaderService get() {
        logger.info("Registering trace metadata to providers");
        TraceMetadataRegistrar.registerServiceTypes(serviceTypeRegistry);
        TraceMetadataRegistrar.registerAnnotationKeys(annotationKeyRegistry);
        return new DefaultTraceMetadataLoaderService(serviceTypeRegistry, annotationKeyRegistry, annotationKeyMatcherRegistry);
    }
}
