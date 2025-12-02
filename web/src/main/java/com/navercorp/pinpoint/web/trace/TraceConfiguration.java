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

package com.navercorp.pinpoint.web.trace;

import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.config.SpanSerializeConfiguration;
import com.navercorp.pinpoint.web.service.ProxyRequestTypeRegistryService;
import com.navercorp.pinpoint.web.trace.callstacks.AnnotationRecordFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@Configuration
@ComponentScan(
        basePackages = {
                "com.navercorp.pinpoint.web.trace.controller",
                "com.navercorp.pinpoint.web.trace.service",

                "com.navercorp.pinpoint.web.trace.dao.hbase",
                "com.navercorp.pinpoint.web.trace.dao.mapper",
        }
)
@Import(
        value = {
                SpanSerializeConfiguration.class,
        }
)
public class TraceConfiguration {

    @Bean
    public AnnotationRecordFormatter annotationRecordFormatter(Optional<ProxyRequestTypeRegistryService> proxyRequestTypeRegistryService) {
        AnnotationRecordFormatter.Builder builder = AnnotationRecordFormatter.newBuilder();
        builder.addDefaultHandlers();

        if (proxyRequestTypeRegistryService.isPresent()) {
            builder.addProxyHeaderAnnotationHeader(proxyRequestTypeRegistryService.get());
        }
        return builder.build();
    }

}
