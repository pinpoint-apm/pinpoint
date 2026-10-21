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

package com.navercorp.pinpoint.web.applicationmap.map;

import com.navercorp.pinpoint.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.io.SpanVersion;
import com.navercorp.pinpoint.loader.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.service.component.DefaultApplicationFactory;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import com.navercorp.pinpoint.web.util.EnvoyTraceMetadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Filtered map of a transaction through the native Envoy tracer, with the ServiceTypes of
 * envoy-type-provider.yml. The root span event (ENVOY_OPERATION) has no destination: with
 * RECORD_STATISTICS it would become a link to an "Unknown" node here.
 */
public class FilteredMapBuilderEnvoyTest {

    private static final int ENVOY = 1550;
    private static final int ENVOY_OPERATION = 1551;
    private static final int ENVOY_INGRESS = 9301;

    private static final String ENVOY_APP = "envoy-proxy";
    private static final String AUTHORITY = "backend:8080";
    private static final long ENVOY_SPAN_ID = 1L;
    private static final long START_TIME = 1000L;

    private static ServiceTypeRegistryService registry;

    @BeforeAll
    static void loadTypeProvider() {
        registry = new DefaultServiceTypeRegistryService(EnvoyTraceMetadata.load().getServiceTypeLocator());
    }

    @Test
    void upstreamWithoutAgentIsAnUnknownNodeNamedByTheAuthority() {
        SpanBo envoy = newEnvoySpan();

        FilteredMap filteredMap = newBuilder().addTransaction(List.of(envoy)).build();

        LinkDataMap source = filteredMap.getLinkDataDuplexMap().getSourceLinkDataMap();
        List<LinkData> envoyLinks = linksFrom(source, ENVOY_APP);
        // one node for both attempts, and none for the root span event
        assertThat(envoyLinks).extracting(LinkData::getToApplication)
                .containsExactly(new Application(AUTHORITY, ServiceType.UNKNOWN));
        assertThat(envoyLinks.get(0).getTotalCount()).isEqualTo(2);
    }

    @Test
    void tracedUpstreamIsLinkedFromEnvoy() {
        SpanBo envoy = newEnvoySpan();
        // only the retry reached the traced upstream
        SpanBo backend = new SpanBo();
        backend.getSpanOwner().setAgentId("backend-agent");
        backend.getSpanOwner().setApplicationName("backend");
        backend.setTransactionId(envoy.getTransactionId());
        backend.setSpanId(3L);
        backend.setParentSpanId(ENVOY_SPAN_ID);
        backend.setServiceType(ServiceType.STAND_ALONE.getCode());
        backend.setApplicationServiceType(ServiceType.STAND_ALONE.getCode());
        backend.setAcceptorHost(AUTHORITY);
        backend.setTraceTime(SpanVersion.TRACE_V2, START_TIME + 40, 15);
        backend.setCollectorAcceptTime(START_TIME + 60);

        FilteredMap filteredMap = newBuilder().addTransaction(List.of(envoy, backend)).build();

        LinkDataMap source = filteredMap.getLinkDataDuplexMap().getSourceLinkDataMap();
        assertThat(linksFrom(source, ENVOY_APP)).extracting(LinkData::getToApplication)
                .containsExactlyInAnyOrder(
                        new Application(AUTHORITY, registry.findServiceType(ENVOY_INGRESS)),
                        new Application(AUTHORITY, ServiceType.UNKNOWN));

        LinkDataMap target = filteredMap.getLinkDataDuplexMap().getTargetLinkDataMap();
        assertThat(linksFrom(target, ENVOY_APP)).extracting(LinkData::getToApplication)
                .containsExactly(new Application("backend", ServiceType.STAND_ALONE));
    }

    private static SpanBo newEnvoySpan() {
        SpanBo span = new SpanBo();
        span.getSpanOwner().setAgentId("envoy-agent");
        span.getSpanOwner().setApplicationName(ENVOY_APP);
        span.setTransactionId(new PinpointServerTraceId("envoy-agent", START_TIME, 1));
        span.setSpanId(ENVOY_SPAN_ID);
        span.setParentSpanId(-1);
        span.setServiceType(ENVOY);
        span.setApplicationServiceType(ENVOY);
        span.setRpc("/api");
        span.setEndPoint("envoy:10000");
        span.setTraceTime(SpanVersion.TRACE_V2, START_TIME, 100);
        span.setCollectorAcceptTime(START_TIME + 110);

        SpanEventBo rootEvent = new SpanEventBo();
        rootEvent.setSequence((short) 0);
        rootEvent.setDepth(1);
        rootEvent.setServiceType(ENVOY_OPERATION);
        rootEvent.setEndElapsed(95);
        span.addSpanEvent(rootEvent);

        span.addSpanEvent(upstreamCall((short) 1, 2L, "10.0.0.5:8080", 5, 30));
        span.addSpanEvent(upstreamCall((short) 2, 3L, "10.0.0.6:8080", 35, 20));
        return span;
    }

    private static SpanEventBo upstreamCall(short sequence, long nextSpanId, String host, int startElapsed, int elapsed) {
        SpanEventBo event = new SpanEventBo();
        event.setSequence(sequence);
        event.setDepth(2);
        event.setServiceType(ENVOY_INGRESS);
        event.setDestinationId(AUTHORITY);
        event.setEndPoint(host);
        event.setNextSpanId(nextSpanId);
        event.setStartElapsed(startElapsed);
        event.setEndElapsed(elapsed);
        return event;
    }

    private static List<LinkData> linksFrom(LinkDataMap linkDataMap, String applicationName) {
        return linkDataMap.getLinkDataList().stream()
                .filter(linkData -> linkData.getFromApplication().getApplicationName().equals(applicationName))
                .toList();
    }

    private static FilteredMapBuilder newBuilder() {
        DefaultApplicationFactory applicationFactory = new DefaultApplicationFactory(registry, mock(ServiceModelResolver.class));
        return new FilteredMapBuilder(applicationFactory, registry, new TimeWindow(Range.between(1, 200000)));
    }
}
