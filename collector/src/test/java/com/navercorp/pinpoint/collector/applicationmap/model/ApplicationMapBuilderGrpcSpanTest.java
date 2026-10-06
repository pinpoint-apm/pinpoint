/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.applicationmap.model;

import com.google.protobuf.TextFormat;
import com.navercorp.pinpoint.collector.uid.service.ServiceLookupService;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.io.DefaultServerHeader;
import com.navercorp.pinpoint.common.server.io.GrpcSpanBinder;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Replays a span the way the collector received it: the protobuf text that
 * {@code GrpcSpanHandler} logs with the "Failed to handle" warning is parsed back into a
 * {@link PSpan}, bound through {@link GrpcSpanBinder} and handed to the builder, so the test covers
 * the binder's empty-string handling together with the builder's fallbacks.
 */
class ApplicationMapBuilderGrpcSpanTest {

    private static final ServiceType GO = ServiceTypeFactory.of(1800, "GO");
    private static final ServiceType KAFKA_CLIENT = ServiceTypeFactory.of(8660, "KAFKA_CLIENT",
            ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);

    private static final String AGENT_ID = "ivs-event-pr";
    private static final String APPLICATION_NAME = "IVS-EVENTPROC-GO-REAL-JP";
    private static final long AGENT_START_TIME = 1788865646904L;
    private static final long REQUEST_TIME = 1791263686787L;

    /**
     * A Kafka consumer span from a hand-written Go instrumentation, as dumped by the collector:
     * a trace root of a QUEUE service type with the topic in endPoint and no acceptorHost or
     * remoteAddr. The agent sends an empty parentInfo on a root span.
     */
    private static final String LOGGED_SPAN = "version: 1 "
            + "transactionId { agentId: \"" + AGENT_ID + "\" agentStartTime: " + AGENT_START_TIME + " sequence: 213581954 } "
            + "spanId: 5245034717718496309 parentSpanId: -1 startTime: 1791263686772 elapsed: 1 apiId: 2 "
            + "serviceType: 8660 "
            + "acceptEvent { rpc: \"consumeMessage\" endPoint: \"ivs-snapshot\" parentInfo { parentApplicationType: 1 } } "
            + "annotation { key: 140 value { stringValue: \"ivs-snapshot\" } } "
            + "applicationServiceType: 1800";

    @Test
    void rootConsumerSpanWithoutAcceptorHostBuildsItsQueueNodeFromTheEndPoint() throws Exception {
        PSpan.Builder pSpan = PSpan.newBuilder();
        TextFormat.merge(LOGGED_SPAN, pSpan);

        ServerHeader header = new DefaultServerHeader(AGENT_ID, AGENT_ID, APPLICATION_NAME, "DEFAULT",
                () -> ServiceUid.DEFAULT, AGENT_START_TIME, ServiceType.UNDEFINED.getCode(), false);
        SpanBo span = new GrpcSpanBinder().bindSpanBo(pSpan.build(), header, REQUEST_TIME);

        // the binder leaves the absent address fields null, which is what reached the builder
        assertThat(span.isRoot()).isTrue();
        assertThat(span.getAcceptorHost()).isNull();
        assertThat(span.getRemoteAddr()).isNull();
        assertThat(span.getEndPoint()).isEqualTo("ivs-snapshot");

        ApplicationMapModel model = newBuilder().build(span);

        final Vertex self = Vertex.of(ServiceUid.DEFAULT.getUid(), APPLICATION_NAME, GO);
        final Vertex queue = Vertex.of(ServiceUid.DEFAULT.getUid(), "ivs-snapshot", KAFKA_CLIENT);
        assertThat(model.getRequestTime()).isEqualTo(REQUEST_TIME);
        assertThat(model.getOutLinks()).containsExactly(new OutLinkRow(queue, self, "_", 1, false));
        assertThat(model.getInLinks()).containsExactly(new InLinkRow(self, queue, "_", 1, false));
        assertThat(model.getResponseTimes()).containsExactly(new ResponseTimeRow(self, AGENT_ID, 1, false));
        // a root span has no parent application, so there is no host mapping to record
        assertThat(model.getAcceptorHosts()).isEmpty();
    }

    private ApplicationMapBuilder newBuilder() {
        ServiceTypeRegistryService registry = Mockito.mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(GO.getCode())).thenReturn(GO);
        when(registry.findServiceType(KAFKA_CLIENT.getCode())).thenReturn(KAFKA_CLIENT);

        ServiceLookupService serviceLookupService = Mockito.mock(ServiceLookupService.class);
        when(serviceLookupService.getServiceUid(Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(ServiceUid.DEFAULT));

        return new ApplicationMapBuilder(registry, serviceLookupService);
    }
}
