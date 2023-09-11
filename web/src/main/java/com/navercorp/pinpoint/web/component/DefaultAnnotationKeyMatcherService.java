/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.component;

import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcherLocator;
import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;

import java.util.Objects;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class DefaultAnnotationKeyMatcherService implements AnnotationKeyMatcherService {

    private final AnnotationKeyMatcherLocator annotationKeyMatcherLocator;

    public DefaultAnnotationKeyMatcherService(TraceMetadataLoaderService typeLoaderService) {
        Objects.requireNonNull(typeLoaderService, "typeLoaderService");
        this.annotationKeyMatcherLocator = typeLoaderService.getAnnotationKeyMatcherLocator();
    }

    @Override
    public AnnotationKeyMatcher findAnnotationKeyMatcher(short serviceType) {
        return annotationKeyMatcherLocator.findAnnotationKeyMatcher(serviceType);
    }
}
