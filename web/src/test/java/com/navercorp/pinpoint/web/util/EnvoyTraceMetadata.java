/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataLoader;
import com.navercorp.pinpoint.common.trace.AnnotationKeyLocator;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcherLocator;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;
import com.navercorp.pinpoint.loader.plugins.trace.yaml.TraceMetadataProviderYamlParser;
import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;

import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * The trace metadata of the native Envoy tracer as the servers load it from
 * pinpoint-external-plugins (envoy-type-provider.yml), with the built-in types.
 */
public final class EnvoyTraceMetadata implements TraceMetadataLoaderService {

    private static final String TYPE_PROVIDER = "META-INF/pinpoint/type-providers/envoy-type-provider.yml";

    private final ServiceTypeLocator serviceTypeLocator;
    private final AnnotationKeyLocator annotationKeyLocator;
    private final AnnotationKeyMatcherLocator annotationKeyMatcherLocator;

    private EnvoyTraceMetadata(TraceMetadataLoader loader) {
        this.serviceTypeLocator = loader.createServiceTypeRegistry();
        this.annotationKeyLocator = loader.createAnnotationKeyRegistry();
        this.annotationKeyMatcherLocator = loader.createAnnotationKeyMatcherRegistry();
    }

    public static EnvoyTraceMetadata load() {
        URL typeProvider = EnvoyTraceMetadata.class.getClassLoader().getResource(TYPE_PROVIDER);
        Objects.requireNonNull(typeProvider, TYPE_PROVIDER);
        TraceMetadataLoader loader = new TraceMetadataLoader();
        loader.load(List.of(new TraceMetadataProviderYamlParser().parse(typeProvider)));
        return new EnvoyTraceMetadata(loader);
    }

    @Override
    public ServiceTypeLocator getServiceTypeLocator() {
        return serviceTypeLocator;
    }

    @Override
    public AnnotationKeyLocator getAnnotationKeyLocator() {
        return annotationKeyLocator;
    }

    @Override
    public AnnotationKeyMatcherLocator getAnnotationKeyMatcherLocator() {
        return annotationKeyMatcherLocator;
    }
}
