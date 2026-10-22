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

import com.navercorp.pinpoint.common.server.bo.ParentApplication;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanOwner;
import com.navercorp.pinpoint.common.server.bo.TraceSourceType;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkEntry;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexWriter;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LinkIndexTraceServiceTest {

    private static final int QUEUE_TYPE = 8660;

    private final LinkIndexWriter writer = mock(LinkIndexWriter.class);
    private final ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
    private final LinkIndexTraceService service = new LinkIndexTraceService(writer, registry);

    private final byte[] traceId = new byte[16];

    @BeforeEach
    void setUp() {
        Arrays.fill(traceId, (byte) 7);
        ServiceType queue = mock(ServiceType.class);
        when(queue.isQueue()).thenReturn(true);
        when(registry.findServiceType(anyInt())).thenReturn(ServiceType.UNKNOWN);
        when(registry.findServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode())).thenReturn(ServiceType.OPENTELEMETRY_SERVER);
        when(registry.findServiceType(ServiceType.OPENTELEMETRY_HTTP_CLIENT.getCode())).thenReturn(ServiceType.OPENTELEMETRY_HTTP_CLIENT);
        when(registry.findServiceType(ServiceType.UNKNOWN_DB.getCode())).thenReturn(ServiceType.UNKNOWN_DB);
        when(registry.findServiceType(QUEUE_TYPE)).thenReturn(queue);
    }

    private SpanOwner owner(String applicationName) {
        SpanOwner owner = new SpanOwner();
        owner.setAgentId("agent");
        owner.setApplicationName(applicationName);
        owner.setServiceUid(() -> ServiceUid.DEFAULT);
        return owner;
    }

    private SpanBo serverSpan() {
        SpanBo span = new SpanBo(TraceSourceType.OPENTELEMETRY, owner("p.cart"));
        span.setTransactionId(new OtelServerTraceId(traceId));
        span.setApplicationServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode());
        span.setServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode());
        span.setSpanId(200);
        span.setParentSpanId(100);
        span.setAcceptorHost("cart:7070");
        span.setElapsed(15);
        return span;
    }

    private static SpanEventBo clientEvent(int serviceType, long nextSpanId, String destinationId) {
        SpanEventBo event = new SpanEventBo();
        event.setServiceType(serviceType);
        event.setNextSpanId(nextSpanId);
        event.setDestinationId(destinationId);
        return event;
    }

    private List<LinkEntry> offered(int times) {
        ArgumentCaptor<LinkEntry> captor = ArgumentCaptor.forClass(LinkEntry.class);
        verify(writer, org.mockito.Mockito.times(times)).offer(captor.capture());
        return captor.getAllValues();
    }

    @Test
    void serverWithoutParentApplication() {
        service.insertSpan(serverSpan());

        LinkEntry entry = offered(1).get(0);
        assertThat(entry.kind()).isEqualTo(LinkKind.SERVER);
        assertThat(entry.traceId()).isEqualTo(traceId);
        assertThat(entry.spanId()).isEqualTo(100);
        assertThat(entry.applicationName()).isEqualTo("p.cart");
        assertThat(entry.serviceType()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
        assertThat(entry.elapsed()).isEqualTo(15);
        assertThat(entry.error()).isFalse();
    }

    @Test
    void serverWithoutEndPointIsRecorded() {
        // Ruby Rack server spans carry http.host only: no endPoint, so no acceptorHost
        SpanBo span = serverSpan();
        span.setAcceptorHost(null);

        service.insertSpan(span);

        assertThat(offered(1).get(0).kind()).isEqualTo(LinkKind.SERVER);
    }

    @Test
    void serverWithParentApplicationIsSkipped() {
        SpanBo span = serverSpan();
        span.setParentApplication(new ParentApplication("p.checkout", ServiceType.OPENTELEMETRY_SERVER.getCode()));

        service.insertSpan(span);

        verify(writer, never()).offer(any());
    }

    @Test
    void rootSpanIsSkipped() {
        SpanBo span = serverSpan();
        span.setParentSpanId(-1);
        span.setAcceptorHost(null);

        service.insertSpan(span);

        verify(writer, never()).offer(any());
    }

    @Test
    void queueSpanIsSkipped() {
        SpanBo span = serverSpan();
        span.setServiceType(QUEUE_TYPE);

        service.insertSpan(span);

        verify(writer, never()).offer(any());
    }

    @Test
    void nonOtelTraceIdIsSkipped() {
        SpanBo span = serverSpan();
        span.setTransactionId(new PinpointServerTraceId("agent", 1L, 1L));

        service.insertSpan(span);

        verify(writer, never()).offer(any());
    }

    @Test
    void rpcClientEventsOfSpanAndChunk() {
        SpanBo span = serverSpan();
        span.setParentApplication(new ParentApplication("p.frontend", ServiceType.OPENTELEMETRY_SERVER.getCode()));
        span.addSpanEvent(clientEvent(ServiceType.OPENTELEMETRY_HTTP_CLIENT.getCode(), 300, "checkout:5050"));
        span.addSpanEvent(clientEvent(ServiceType.UNKNOWN_DB.getCode(), 301, "orders-db"));          // database
        span.addSpanEvent(clientEvent(ServiceType.OPENTELEMETRY_HTTP_CLIENT.getCode(), -1, "x:80"));  // no next span
        span.addSpanEvent(clientEvent(ServiceType.OPENTELEMETRY_HTTP_CLIENT.getCode(), 302, null));   // no destination
        service.insertSpan(span);

        SpanChunkBo chunk = new SpanChunkBo(TraceSourceType.OPENTELEMETRY, owner("p.frontend"));
        chunk.setTransactionId(new OtelServerTraceId(traceId));
        chunk.setApplicationServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode());
        chunk.addSpanEvent(clientEvent(ServiceType.OPENTELEMETRY_HTTP_CLIENT.getCode(), 400, "currency:7001"));
        service.insertSpanChunk(chunk);

        List<LinkEntry> entries = offered(2);
        assertThat(entries).extracting(LinkEntry::kind).containsOnly(LinkKind.CLIENT);
        assertThat(entries).extracting(LinkEntry::spanId).containsExactly(300L, 400L);
        assertThat(entries).extracting(LinkEntry::destinationId).containsExactly("checkout:5050", "currency:7001");
        assertThat(entries).extracting(LinkEntry::applicationName).containsOnly("p.cart", "p.frontend");
    }

    @Test
    void everyRpcClientEventIsOfferedTheWriterDecidesSuppression() {
        SpanBo span = serverSpan();
        span.addSpanEvent(clientEvent(ServiceType.OPENTELEMETRY_HTTP_CLIENT.getCode(), 300, "checkout:5050"));
        span.addSpanEvent(clientEvent(ServiceType.OPENTELEMETRY_HTTP_CLIENT.getCode(), 301, "checkout:5050"));

        service.insertSpan(span);

        assertThat(offered(3)).extracting(LinkEntry::spanId).containsExactly(100L, 300L, 301L);
    }
}
