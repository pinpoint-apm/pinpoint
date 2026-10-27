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
import com.navercorp.pinpoint.otlp.trace.linkindex.SlotQuota;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.LinkIndexSuppression;
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
            // suppression on by default for both kinds: list read from the store, counters registered per kind
            assertThat(context.getBean(LinkIndexSuppression.class).toString())
                    .contains("store=true").contains("probePerSlot=100").contains("serverEnabled=true");
            assertThat(registry.find("collector.otlptrace.linkindex.suppressed").functionCounters()).hasSize(2);
            assertThat(registry.find("collector.otlptrace.linkindex.suppressed").tag("kind", "server").functionCounter()).isNotNull();
            assertThat(registry.find("collector.otlptrace.linkindex.probed").tag("kind", "client").functionCounter()).isNotNull();
            assertThat(registry.find("collector.otlptrace.linkindex.suppress.active").gauge()).isNotNull();
            assertThat(registry.find("collector.otlptrace.linkindex.suppress.active.server").gauge()).isNotNull();
            assertThat(registry.find("collector.otlptrace.linkindex.suppress.refresh.errors").functionCounter()).isNotNull();
            // quota: 20 % per CLIENT identifier by default, SERVER limit off; drops tagged by reason and kind
            assertThat(context.getBean(SlotQuota.class).toString())
                    .contains("maxEntriesPerSlot=1000000").contains("clientPercent=20").contains("serverPercent=100");
            assertThat(registry.find("collector.otlptrace.linkindex.dropped").functionCounters()).hasSize(8);
            assertThat(registry.find("collector.otlptrace.linkindex.dropped")
                    .tag("reason", "identifier_limit").tag("kind", "client").functionCounter()).isNotNull();
            assertThat(registry.find("collector.otlptrace.linkindex.slot.entries").gauge()).isNotNull();
            assertThat(registry.find("collector.otlptrace.linkindex.slot.limit.reached.second").gauge()).isNotNull();
            assertThat(registry.find("collector.otlptrace.linkindex.quota.limited.identifiers").gauge()).isNotNull();
            assertThat(registry.find("collector.otlptrace.linkindex.quota.untracked").functionCounter()).isNotNull();
        }
    }

    @Test
    void serverSuppressionCanBeTurnedOff() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("pinpoint.collector.otlptrace.link-index.enabled", "true");
        properties.put("pinpoint.collector.otlptrace.link-index.suppress.server.enabled", "false");
        try (AnnotationConfigApplicationContext context = context(properties, true)) {
            assertThat(context.getBean(LinkIndexSuppression.class).isServerEnabled()).isFalse();
        }
    }

    @Test
    void quotaSettings() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("pinpoint.collector.otlptrace.link-index.enabled", "true");
        properties.put("pinpoint.collector.otlptrace.link-index.max-entries-per-slot", "5000");
        properties.put("pinpoint.collector.otlptrace.link-index.max-entries-per-identifier-percent", "10");
        properties.put("pinpoint.collector.otlptrace.link-index.max-server-entries-per-application-percent", "50");
        try (AnnotationConfigApplicationContext context = context(properties, true)) {
            assertThat(context.getBean(SlotQuota.class).toString())
                    .contains("maxEntriesPerSlot=5000").contains("clientPercent=10").contains("serverPercent=50");
        }
    }

    @Test
    void quotaPercentOutOfRangeFailsToStart() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("pinpoint.collector.otlptrace.link-index.enabled", "true");
        properties.put("pinpoint.collector.otlptrace.link-index.max-entries-per-identifier-percent", "0");
        assertThatThrownBy(() -> context(properties, true))
                .isInstanceOf(BeanCreationException.class)
                .rootCause()
                .hasMessageContaining("1..100");
    }

    @Test
    void suppressionDisabledKeepsOnlyTheStaticRules() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("pinpoint.collector.otlptrace.link-index.enabled", "true");
        properties.put("pinpoint.collector.otlptrace.link-index.suppress.enabled", "false");
        properties.put("pinpoint.collector.otlptrace.link-index.suppress.probe-per-slot", "1");
        properties.put("pinpoint.collector.otlptrace.link-index.suppress.force", "mapmatch-bg|navi:12000");
        properties.put("pinpoint.collector.otlptrace.link-index.suppress.pin", "p.frontend");
        try (AnnotationConfigApplicationContext context = context(properties, true)) {
            LinkIndexSuppression suppression = context.getBean(LinkIndexSuppression.class);
            assertThat(suppression.toString()).contains("store=false").contains("probePerSlot=1")
                    .contains("mapmatch-bg|navi:12000").contains("p.frontend");
            assertThat(suppression.allow("mapmatch-bg", "navi:12000", 1)).isTrue();    // probe
            assertThat(suppression.allow("mapmatch-bg", "navi:12000", 1)).isFalse();
        }
    }
}
