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

package com.navercorp.pinpoint.collector.applicationmap.model;

import com.navercorp.pinpoint.collector.uid.service.ServiceLookupService;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.bo.ExceptionInfo;
import com.navercorp.pinpoint.common.server.bo.ParentApplication;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataLoader;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.plugins.trace.yaml.TraceMetadataProviderYamlParser;
import com.navercorp.pinpoint.loader.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Builds the map from spans of the native Envoy tracer, with the ServiceTypes of
 * envoy-type-provider.yml: the root span event (ENVOY_OPERATION) has no destination, and each
 * upstream attempt (ENVOY_INGRESS / ENVOY_EGRESS) has the upstream authority as its destination
 * and the upstream host as its endPoint.
 */
class ApplicationMapBuilderEnvoyTest {

    private static final String ENVOY_TYPE_PROVIDER = "META-INF/pinpoint/type-providers/envoy-type-provider.yml";

    private static final int ENVOY = 1550;
    private static final int ENVOY_OPERATION = 1551;
    private static final int ENVOY_INGRESS = 9301;
    private static final int ENVOY_EGRESS = 9302;

    private static final String ENVOY_APP = "envoy-proxy";
    private static final String ENVOY_END_POINT = "envoy:10000";
    private static final String AUTHORITY = "backend:8080";
    private static final long ACCEPT_TIME = 1000L;

    private static ServiceTypeRegistryService registry;

    @BeforeAll
    static void loadTypeProvider() {
        // the definitions the servers load from pinpoint-external-plugins, with the built-in types
        URL typeProvider = ApplicationMapBuilderEnvoyTest.class.getClassLoader().getResource(ENVOY_TYPE_PROVIDER);
        assertThat(typeProvider).isNotNull();
        TraceMetadataLoader loader = new TraceMetadataLoader();
        loader.load(List.of(new TraceMetadataProviderYamlParser().parse(typeProvider)));
        registry = new DefaultServiceTypeRegistryService(loader.createServiceTypeRegistry());
    }

    @Test
    void envoyServiceTypes() {
        ServiceType envoy = registry.findServiceType(ENVOY);
        assertThat(envoy.getName()).isEqualTo("ENVOY");
        assertThat(envoy.isWas()).isTrue();
        assertThat(envoy.isRecordStatistics()).isTrue();

        ServiceType operation = registry.findServiceType(ENVOY_OPERATION);
        assertThat(operation.getName()).isEqualTo("ENVOY_OPERATION");
        assertThat(operation.isRecordStatistics()).isFalse();

        for (int code : new int[]{ENVOY_INGRESS, ENVOY_EGRESS}) {
            ServiceType upstream = registry.findServiceType(code);
            assertThat(upstream.isRpcClient()).isTrue();
            assertThat(upstream.isRecordStatistics()).isTrue();
            assertThat(upstream.isTerminal()).isFalse();
        }
    }

    @Test
    void eachUpstreamAttemptLinksToTheAuthority() {
        SpanBo span = newEnvoySpan();
        span.setParentSpanId(-1);
        span.addSpanEvent(rootEvent());
        // the first attempt fails (e.g. UF) and the retry succeeds on another host
        span.addSpanEvent(upstreamCall(ENVOY_INGRESS, "10.0.0.5:8080", 30, true));
        span.addSpanEvent(upstreamCall(ENVOY_INGRESS, "10.0.0.6:8080", 20, false));

        ApplicationMapModel model = newBuilder().build(span);

        final Vertex envoy = envoyVertex();
        final Vertex user = Vertex.of(envoy.serviceUid(), ENVOY_APP, ServiceType.USER);
        final Vertex backend = Vertex.of(envoy.serviceUid(), AUTHORITY, registry.findServiceType(ENVOY_INGRESS));

        // no link for the root span event, one per attempt to the authority
        assertThat(model.getOutLinks()).containsExactly(
                new OutLinkRow(envoy, backend, "10.0.0.5:8080", 30, true),
                new OutLinkRow(envoy, backend, "10.0.0.6:8080", 20, false));
        assertThat(model.getInLinks()).containsExactly(
                new InLinkRow(envoy, user, "_", 100, false),
                new InLinkRow(backend, envoy, ENVOY_END_POINT, 30, true),
                new InLinkRow(backend, envoy, ENVOY_END_POINT, 20, false));
        assertThat(model.getResponseTimes()).containsExactly(
                new ResponseTimeRow(envoy, "envoy-agent", 100, false));
        assertThat(model.getAcceptorHosts()).isEmpty();
    }

    @Test
    void upstreamCallsOfAnAsyncSpan() {
        // spawn_upstream_span, ext_authz, ext_proc: the same events under the async event
        SpanEventBo asyncEvent = new SpanEventBo();
        asyncEvent.setServiceType(ServiceType.ASYNC.getCode());
        asyncEvent.setDepth(1);

        SpanChunkBo spanChunk = new SpanChunkBo();
        spanChunk.getSpanOwner().setAgentId("envoy-agent");
        spanChunk.getSpanOwner().setApplicationName(ENVOY_APP);
        spanChunk.setApplicationServiceType(ENVOY);
        spanChunk.setEndPoint(ENVOY_END_POINT);
        spanChunk.setCollectorAcceptTime(ACCEPT_TIME);
        spanChunk.addSpanEventBoList(List.of(asyncEvent, rootEvent(),
                upstreamCall(ENVOY_EGRESS, "10.0.0.5:8080", 20, false)));

        ApplicationMapModel model = newBuilder().build(spanChunk);

        final Vertex envoy = envoyVertex();
        final Vertex backend = Vertex.of(envoy.serviceUid(), AUTHORITY, registry.findServiceType(ENVOY_EGRESS));
        assertThat(model.getOutLinks()).containsExactly(
                new OutLinkRow(envoy, backend, "10.0.0.5:8080", 20, false));
        assertThat(model.getInLinks()).containsExactly(
                new InLinkRow(backend, envoy, ENVOY_END_POINT, 20, false));
        assertThat(model.getResponseTimes()).isEmpty();
    }

    @Test
    void tracedUpstreamMapsTheAuthorityToItsApplication() {
        // the span of a traced upstream: Envoy sent the destination (the authority) as Pinpoint-Host,
        // which the web looks up to replace the authority node with this application
        SpanBo span = new SpanBo();
        span.getSpanOwner().setAgentId("backend-agent");
        span.getSpanOwner().setApplicationName("backend");
        span.setServiceType(ServiceType.STAND_ALONE.getCode());
        span.setApplicationServiceType(ServiceType.STAND_ALONE.getCode());
        span.setParentSpanId(10);
        span.setAcceptorHost(AUTHORITY);
        span.setParentApplication(ParentApplication.of(null, ENVOY_APP, ENVOY));
        span.setElapsed(15);
        span.setCollectorAcceptTime(ACCEPT_TIME);

        ApplicationMapModel model = newBuilder().build(span);

        final Vertex envoy = envoyVertex();
        final Vertex backend = Vertex.of(envoy.serviceUid(), "backend", ServiceType.STAND_ALONE);
        assertThat(model.getAcceptorHosts()).containsExactly(
                new AcceptorHostRow(envoy, backend, AUTHORITY));
        assertThat(model.getInLinks()).containsExactly(
                new InLinkRow(backend, envoy, "_", 15, false));
    }

    private SpanBo newEnvoySpan() {
        SpanBo span = new SpanBo();
        span.getSpanOwner().setAgentId("envoy-agent");
        span.getSpanOwner().setApplicationName(ENVOY_APP);
        span.setServiceType(ENVOY);
        span.setApplicationServiceType(ENVOY);
        span.setRpc("/api");
        span.setElapsed(100);
        span.setEndPoint(ENVOY_END_POINT);
        span.setCollectorAcceptTime(ACCEPT_TIME);
        return span;
    }

    private static SpanEventBo rootEvent() {
        SpanEventBo event = new SpanEventBo();
        event.setServiceType(ENVOY_OPERATION);
        event.setDepth(1);
        event.setEndElapsed(90);
        return event;
    }

    private static SpanEventBo upstreamCall(int serviceType, String host, int elapsed, boolean error) {
        SpanEventBo event = new SpanEventBo();
        event.setServiceType(serviceType);
        event.setDepth(2);
        event.setDestinationId(AUTHORITY);
        event.setEndPoint(host);
        event.setEndElapsed(elapsed);
        if (error) {
            event.setExceptionInfo(new ExceptionInfo(1, "UF"));
        }
        return event;
    }

    private Vertex envoyVertex() {
        return Vertex.of(ServiceUid.DEFAULT.getUid(), ENVOY_APP, registry.findServiceType(ENVOY));
    }

    private ApplicationMapBuilder newBuilder() {
        ServiceLookupService serviceLookupService = Mockito.mock(ServiceLookupService.class);
        when(serviceLookupService.getServiceUid(Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(ServiceUid.DEFAULT));
        return new ApplicationMapBuilder(registry, serviceLookupService);
    }
}
