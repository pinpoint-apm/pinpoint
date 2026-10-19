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

package com.navercorp.pinpoint.otlp.trace.collector.linkindex;

import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexWriter;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexConnection;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexRedisConnector;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexRedisConnectorFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OtlpLinkIndexModuleTest {

    private static AnnotationConfigApplicationContext context(Map<String, Object> properties, boolean withFactory) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        // @Value Duration ("5m") needs Spring Boot's conversion service outside a Boot application
        context.getBeanFactory().setConversionService(ApplicationConversionService.getSharedInstance());
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
        context.registerBean(ServiceTypeRegistryService.class, () -> mock(ServiceTypeRegistryService.class));
        context.registerBean(MeterRegistry.class, SimpleMeterRegistry::new);
        if (withFactory) {
            context.registerBean(LinkIndexRedisConnectorFactory.class, OtlpLinkIndexModuleTest::stubFactory);
        }
        context.register(OtlpLinkIndexModule.class);
        context.refresh();
        return context;
    }

    private static LinkIndexRedisConnectorFactory stubFactory() {
        LinkIndexRedisConnector connector = mock(LinkIndexRedisConnector.class);
        when(connector.connect()).thenReturn(mock(LinkIndexConnection.class));
        return properties -> connector;
    }

    @Test
    void disabledByDefault() {
        try (AnnotationConfigApplicationContext context = context(Map.of(), false)) {
            assertThat(context.getBeansOfType(LinkIndexWriter.class)).isEmpty();
            assertThat(context.getBeansOfType(TraceService.class)).isEmpty();
        }
    }

    @Test
    void enabledWithoutStoreFailsToStart() {
        assertThatThrownBy(() -> context(Map.of("pinpoint.collector.otlptrace.link-index.enabled", "true"), false))
                .isInstanceOf(BeanCreationException.class)
                .rootCause()
                .hasMessageContaining("pinpoint.collector.otlptrace.link-index.redis.nodes is empty");
    }

    @Test
    void connectorFactoryBeanReplacesTheProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("pinpoint.collector.otlptrace.link-index.enabled", "true");
        properties.put("pinpoint.collector.otlptrace.link-index.buckets", "1024");
        properties.put("pinpoint.collector.otlptrace.link-index.ttl", "2m");
        try (AnnotationConfigApplicationContext context = context(properties, true)) {
            assertThat(context.getBean(LinkIndexWriter.class)).isNotNull();
            assertThat(context.getBean(TraceService.class)).isInstanceOf(LinkIndexTraceService.class);
            MeterRegistry registry = context.getBean(MeterRegistry.class);
            assertThat(registry.find("collector.otlptrace.linkindex.written").functionCounter()).isNotNull();
            assertThat(registry.find("collector.otlptrace.linkindex.dropped").tag("reason", "slot_limit").functionCounter()).isNotNull();
        }
    }
}
