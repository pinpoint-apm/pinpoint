/*
 * Copyright 2019 NAVER Corp.
 *
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

package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcherLocator;
import com.navercorp.pinpoint.common.trace.AnnotationKeyLocator;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class DefaultTraceMetadataLoaderService implements TraceMetadataLoaderService {

    private final ServiceTypeLocator serviceTypeLocator;
    private final AnnotationKeyLocator annotationKeyLocator;
    private final AnnotationKeyMatcherLocator annotationKeyMatcherLocator;

    public DefaultTraceMetadataLoaderService(ServiceTypeLocator serviceTypeLocator, AnnotationKeyLocator annotationKeyLocator, AnnotationKeyMatcherLocator annotationKeyMatcherLocator) {
        this.serviceTypeLocator = Assert.requireNonNull(serviceTypeLocator, "serviceTypeLocator");
        this.annotationKeyLocator = Assert.requireNonNull(annotationKeyLocator, "annotationKeyLocator");
        this.annotationKeyMatcherLocator = Assert.requireNonNull(annotationKeyMatcherLocator, "annotationKeyMatcherLocator");
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
