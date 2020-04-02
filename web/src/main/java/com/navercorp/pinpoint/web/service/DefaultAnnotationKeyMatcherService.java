/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcherLocator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
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
