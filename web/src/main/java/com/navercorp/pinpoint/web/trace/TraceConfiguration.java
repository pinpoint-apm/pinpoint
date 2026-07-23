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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.buffer.StringAllocatorFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.config.SpanSerializeConfiguration;
import com.navercorp.pinpoint.web.service.ProxyRequestTypeRegistryService;
import com.navercorp.pinpoint.web.trace.callstacks.AnnotationRecordFormatter;
import com.navercorp.pinpoint.web.trace.callstacks.AttributeBoWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@Configuration
@Import(
        value = {
                SpanSerializeConfiguration.class
        }
)
public class TraceConfiguration {

    @Configuration
    @ComponentScan(basePackages = {
            "com.navercorp.pinpoint.web.trace.controller",
    })
    public static class TraceWebConfiguration {
    }

    @Configuration
    @ComponentScan(
            basePackages = {
                    "com.navercorp.pinpoint.web.trace.service",
                    "com.navercorp.pinpoint.web.trace.dao.hbase",
                    "com.navercorp.pinpoint.web.trace.dao.mapper",
            }
    )
    public static class TraceServiceConfiguration {
        /**
         * Per-row string allocation strategy for the trace row mappers.
         */
        @Bean
        public StringAllocatorFactory stringAllocatorFactory(@Value("${web.hbase.mapper.cache.string.size:-1}") int stringCacheSize) {
            if (stringCacheSize > 0) {
                return StringAllocatorFactory.cached(stringCacheSize);
            }
            return StringAllocatorFactory.DEFAULT;
        }

        @Bean
        public AnnotationRecordFormatter annotationRecordFormatter(Optional<ProxyRequestTypeRegistryService> proxyRequestTypeRegistryService) {
            AnnotationRecordFormatter.Builder builder = AnnotationRecordFormatter.newBuilder();
            builder.addDefaultHandlers();

            if (proxyRequestTypeRegistryService.isPresent()) {
                builder.addProxyHeaderAnnotationHeader(proxyRequestTypeRegistryService.get());
            }
            return builder.build();
        }

        // RecorderFactoryProvider (scanned above) requires this bean; keep it in this nested config
        // so contexts importing only TraceServiceConfiguration (e.g. batch) can start.
        @Bean
        public AttributeBoWriter attributeBoWriter(ObjectMapper mapper) {
            return new AttributeBoWriter(mapper);
        }
    }
}
