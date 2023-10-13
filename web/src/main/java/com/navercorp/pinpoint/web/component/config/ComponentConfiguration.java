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

package com.navercorp.pinpoint.web.component.config;

import com.navercorp.pinpoint.common.server.config.TypeLoaderConfiguration;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.DefaultAnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.web.component.AnnotationKeyMatcherService;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.component.DefaultAnnotationKeyMatcherService;
import com.navercorp.pinpoint.web.component.DefaultApplicationFactory;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TypeLoaderConfiguration.class)
public class ComponentConfiguration {

    @Bean
    public ApplicationFactory applicationFactory(ServiceTypeRegistryService registry) {
        return new DefaultApplicationFactory(registry);
    }

    @Bean
    public AnnotationKeyRegistryService annotationKeyRegistryService(TraceMetadataLoaderService typeLoaderService) {
        return new DefaultAnnotationKeyRegistryService(typeLoaderService);
    }

    @Bean
    public RangeFactory rangeFactory(TimeSlot timeSlot) {
        return new RangeFactory(timeSlot);
    }

    @Bean
    public AnnotationKeyMatcherService annotationKeyMatcherService(TraceMetadataLoaderService typeLoaderService) {
        return new DefaultAnnotationKeyMatcherService(typeLoaderService);
    }

}
