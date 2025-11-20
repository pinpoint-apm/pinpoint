/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.trace.ApiParserProvider;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.component.AnnotationKeyMatcherService;
import com.navercorp.pinpoint.web.vo.callstacks.AnnotationRecordFormatter;
import com.navercorp.pinpoint.web.vo.callstacks.RecordFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RecorderFactoryProvider {

    private final ServiceTypeRegistryService registry;

    private final AnnotationKeyMatcherService annotationKeyMatcherService;

    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    private final AnnotationRecordFormatter annotationRecordFormatter;

    private final ApiParserProvider apiParserProvider;

    public RecorderFactoryProvider(ServiceTypeRegistryService registry,
                                   AnnotationKeyMatcherService annotationKeyMatcherService,
                                   AnnotationKeyRegistryService annotationKeyRegistryService,
                                   AnnotationRecordFormatter annotationRecordFormatter,
                                   ApiParserProvider apiParserProvider) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.annotationKeyMatcherService = Objects.requireNonNull(annotationKeyMatcherService, "annotationKeyMatcherService");
        this.annotationKeyRegistryService = Objects.requireNonNull(annotationKeyRegistryService, "annotationKeyRegistryService");
        this.annotationRecordFormatter = Objects.requireNonNull(annotationRecordFormatter, "annotationRecordFormatter");
        this.apiParserProvider = Objects.requireNonNull(apiParserProvider, "apiParserRegistry");
    }

    public RecordFactory getRecordFactory()  {
        return new RecordFactory(annotationKeyMatcherService, registry, annotationKeyRegistryService, annotationRecordFormatter, apiParserProvider);
    }
}
