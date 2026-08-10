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

import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.bo.ParentApplication;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ApplicationMapBuilderTest {

    private static final ServiceType APP_TYPE = ServiceTypeFactory.of(1210, "TEST_APP");
    private static final ServiceType RPC_TYPE = ServiceTypeFactory.of(9055, "TEST_RPC_CLIENT",
            ServiceTypeProperty.TERMINAL, ServiceTypeProperty.RECORD_STATISTICS, ServiceTypeProperty.INCLUDE_DESTINATION_ID);
    private static final ServiceType INTERNAL_TYPE = ServiceTypeFactory.of(5011, "TEST_INTERNAL");

    private static final long ACCEPT_TIME = 1000L;

    private ApplicationMapBuilder builder;

    @BeforeEach
    void setUp() {
        ServiceTypeRegistryService registry = Mockito.mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(APP_TYPE.getCode())).thenReturn(APP_TYPE);
        when(registry.findServiceType(RPC_TYPE.getCode())).thenReturn(RPC_TYPE);
        when(registry.findServiceType(INTERNAL_TYPE.getCode())).thenReturn(INTERNAL_TYPE);

        this.builder = new ApplicationMapBuilder(registry);
    }

    private SpanBo newSpan() {
        SpanBo span = new SpanBo();
        span.getSpanOwner().setAgentId("agent1");
        span.getSpanOwner().setApplicationName("app1");
        span.setServiceType(APP_TYPE.getCode());
        span.setApplicationServiceType(APP_TYPE.getCode());
        span.setElapsed(100);
        span.setEndPoint("self:8080");
        span.setCollectorAcceptTime(ACCEPT_TIME);
        return span;
    }

    private SpanEventBo newRpcEvent() {
        SpanEventBo event = new SpanEventBo();
        event.setServiceType(RPC_TYPE.getCode());
        event.setDestinationId("dest");
        event.setEndPoint("dest:8080");
        event.setEndElapsed(20);
        return event;
    }

    private Vertex selfVertex(SpanBo span) {
        return Vertex.of(span.getServiceUid().getUid(), "app1", APP_TYPE);
    }

    @Test
    void buildRootSpan() {
        SpanBo span = newSpan();
        span.setParentSpanId(-1);
        span.addSpanEvent(newRpcEvent());

        ApplicationMapModel model = builder.build(span);

        assertThat(model.getRequestTime()).isEqualTo(ACCEPT_TIME);

        final Vertex self = selfVertex(span);
        final Vertex user = Vertex.of(self.serviceUid(), "app1", ServiceType.USER);
        final Vertex dest = Vertex.of(self.serviceUid(), "dest", RPC_TYPE);

        // root span: self <- USER, span event: dest <- self
        assertThat(model.getInLinks()).containsExactly(
                new InLinkRow(self, user, "_", 100, false),
                new InLinkRow(dest, self, "self:8080", 20, false));

        // span event: self -> dest
        assertThat(model.getOutLinks()).containsExactly(
                new OutLinkRow(self, "_", dest, "dest:8080", 20, false));

        assertThat(model.getResponseTimes()).containsExactly(
                new ResponseTimeRow(self, "agent1", 100, false));

        assertThat(model.getAcceptorHosts()).isEmpty();
    }

    @Test
    void buildChildSpan() {
        SpanBo span = newSpan();
        span.setParentSpanId(10);
        span.setAcceptorHost("self.example.com");
        span.setParentApplication(ParentApplication.of(null, "parentApp", APP_TYPE.getCode()));
        span.setErrCode(1);

        ApplicationMapModel model = builder.build(span);

        final Vertex self = selfVertex(span);
        final Vertex parent = Vertex.of(self.serviceUid(), "parentApp", APP_TYPE);

        assertThat(model.getAcceptorHosts()).containsExactly(
                new AcceptorHostRow(parent, self, "self.example.com"));

        assertThat(model.getInLinks()).containsExactly(
                new InLinkRow(self, parent, "_", 100, true));

        assertThat(model.getOutLinks()).isEmpty();

        assertThat(model.getResponseTimes()).containsExactly(
                new ResponseTimeRow(self, "agent1", 100, true));
    }

    @Test
    void buildChildSpanWithoutParentApplication() {
        SpanBo span = newSpan();
        span.setParentSpanId(10);

        ApplicationMapModel model = builder.build(span);

        // invalid span: no links, response time only
        assertThat(model.getInLinks()).isEmpty();
        assertThat(model.getOutLinks()).isEmpty();
        assertThat(model.getResponseTimes()).hasSize(1);
    }

    @Test
    void buildSpanEventStatSkipsNonStatisticsType() {
        SpanBo span = newSpan();
        span.setParentSpanId(-1);
        SpanEventBo internalEvent = new SpanEventBo();
        internalEvent.setServiceType(INTERNAL_TYPE.getCode());
        span.addSpanEvent(internalEvent);

        ApplicationMapModel model = builder.build(span);

        // only the root span's USER in-link, no event links
        assertThat(model.getInLinks()).hasSize(1);
        assertThat(model.getOutLinks()).isEmpty();
    }

    @Test
    void buildSpanChunk() {
        SpanChunkBo spanChunk = new SpanChunkBo();
        spanChunk.getSpanOwner().setAgentId("agent1");
        spanChunk.getSpanOwner().setApplicationName("app1");
        spanChunk.setApplicationServiceType(APP_TYPE.getCode());
        spanChunk.setEndPoint("self:8080");
        spanChunk.setCollectorAcceptTime(ACCEPT_TIME);
        spanChunk.addSpanEventBoList(List.of(newRpcEvent()));

        ApplicationMapModel model = builder.build(spanChunk);

        final Vertex self = Vertex.of(spanChunk.getServiceUid().getUid(), "app1", APP_TYPE);
        final Vertex dest = Vertex.of(self.serviceUid(), "dest", RPC_TYPE);

        assertThat(model.getOutLinks()).containsExactly(
                new OutLinkRow(self, "_", dest, "dest:8080", 20, false));
        assertThat(model.getInLinks()).containsExactly(
                new InLinkRow(dest, self, "self:8080", 20, false));
        assertThat(model.getResponseTimes()).isEmpty();
        assertThat(model.getAcceptorHosts()).isEmpty();
    }

    @Test
    void buildEmptySpanChunk() {
        SpanChunkBo spanChunk = new SpanChunkBo();
        spanChunk.setCollectorAcceptTime(ACCEPT_TIME);

        ApplicationMapModel model = builder.build(spanChunk);

        assertThat(model.isEmpty()).isTrue();
        assertThat(model.getRequestTime()).isEqualTo(ACCEPT_TIME);
    }
}
