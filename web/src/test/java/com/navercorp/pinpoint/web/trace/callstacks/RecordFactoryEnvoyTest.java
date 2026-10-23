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

package com.navercorp.pinpoint.web.trace.callstacks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.trace.ApiParserProvider;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.io.SpanVersion;
import com.navercorp.pinpoint.loader.service.DefaultAnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.web.component.DefaultAnnotationKeyMatcherService;
import com.navercorp.pinpoint.web.trace.span.CallTreeNode;
import com.navercorp.pinpoint.web.trace.span.SpanAlign;
import com.navercorp.pinpoint.web.trace.span.SpanEventAlign;
import com.navercorp.pinpoint.web.util.EnvoyTraceMetadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Call tree records of a span of the native Envoy tracer, with the keys of envoy-type-provider.yml.
 */
public class RecordFactoryEnvoyTest {

    private static final int ENVOY = 1550;
    private static final int ENVOY_OPERATION = 1551;
    private static final int ENVOY_INGRESS = 9301;

    private static EnvoyTraceMetadata metadata;

    @BeforeAll
    static void loadTypeProvider() {
        metadata = EnvoyTraceMetadata.load();
    }

    @Test
    void envoySpanEvents() {
        SpanBo span = new SpanBo();
        span.getSpanOwner().setAgentId("envoy-agent");
        span.getSpanOwner().setApplicationName("envoy-proxy");
        span.setTransactionId(new PinpointServerTraceId("envoy-agent", 1000L, 1));
        span.setServiceType(ENVOY);
        span.setApplicationServiceType(ENVOY);
        span.setRpc("/api");
        span.setTraceTime(SpanVersion.TRACE_V2, 1000L, 100);

        SpanEventBo rootEvent = new SpanEventBo();
        rootEvent.setDepth(1);
        rootEvent.setServiceType(ENVOY_OPERATION);
        rootEvent.addAnnotation(AnnotationBo.of(AnnotationKey.HTTP_STATUS_CODE.getCode(), 503));
        rootEvent.addAnnotation(AnnotationBo.of(9442, "backend"));
        rootEvent.addAnnotation(AnnotationBo.of(9444, "URX"));
        rootEvent.addAnnotation(AnnotationBo.of(9445, "8b7c4a5e-6f0d-4c43-9a39-0e3b6b1f6d21"));
        rootEvent.addAnnotation(AnnotationBo.of(9443, new StringStringValue("grpc.status_code", "14")));
        rootEvent.addAnnotation(AnnotationBo.of(9446, new StringStringValue("last_downstream_rx_byte_received", "0.212")));

        SpanEventBo upstreamCall = new SpanEventBo();
        upstreamCall.setDepth(2);
        upstreamCall.setServiceType(ENVOY_INGRESS);
        upstreamCall.setDestinationId("backend:8080");
        upstreamCall.setEndPoint("10.0.0.6:8080");
        upstreamCall.addAnnotation(AnnotationBo.of(AnnotationKey.HTTP_URL.getCode(), "/api"));
        upstreamCall.addAnnotation(AnnotationBo.of(9443, new StringStringValue("retry.count", "1")));

        CallTreeNode spanNode = new CallTreeNode(null, new SpanAlign(span));
        CallTreeNode rootEventNode = new CallTreeNode(spanNode, new SpanEventAlign(span, rootEvent));
        CallTreeNode upstreamCallNode = new CallTreeNode(rootEventNode, new SpanEventAlign(span, upstreamCall));

        RecordFactory factory = newRecordFactory();
        factory.get(spanNode);

        Record root = factory.get(rootEventNode);
        assertThat(root.getApiType()).isEqualTo("ENVOY_OPERATION");
        assertThat(root.getArguments()).isEmpty();
        assertThat(factory.getAnnotations(2, root.getId(), rootEventNode.getAlign()))
                .extracting(Record::getTitle, Record::getArguments)
                .containsExactly(
                        tuple("http.status.code", "503"),
                        tuple("upstream.cluster", "backend"),
                        tuple("envoy.response_flags", "URX"),
                        tuple("envoy.x_request_id", "8b7c4a5e-6f0d-4c43-9a39-0e3b6b1f6d21"),
                        tuple("envoy.tag", "grpc.status_code=14"),
                        tuple("envoy.event", "last_downstream_rx_byte_received=0.212"));

        // http.url is not shown in the record set, so the matcher puts it in the arguments
        Record call = factory.get(upstreamCallNode);
        assertThat(call.getApiType()).isEqualTo("ENVOY_INGRESS");
        assertThat(call.getArguments()).isEqualTo("/api");
        assertThat(factory.getAnnotations(3, call.getId(), upstreamCallNode.getAlign()))
                .extracting(Record::getTitle, Record::getArguments)
                .containsExactly(tuple("envoy.tag", "retry.count=1"));
    }

    private static RecordFactory newRecordFactory() {
        AnnotationRecordFormatter.Builder formatter = AnnotationRecordFormatter.newBuilder();
        formatter.addDefaultHandlers();
        return new RecordFactory(new DefaultAnnotationKeyMatcherService(metadata),
                new DefaultServiceTypeRegistryService(metadata.getServiceTypeLocator()),
                new DefaultAnnotationKeyRegistryService(metadata),
                formatter.build(),
                new ApiParserProvider(),
                new AttributeBoWriter(new ObjectMapper()));
    }
}
