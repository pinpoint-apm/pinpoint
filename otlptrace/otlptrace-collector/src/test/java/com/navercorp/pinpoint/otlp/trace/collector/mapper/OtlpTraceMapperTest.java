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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.ActiveMQMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.KafkaMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.OtlpMessagingConsumerResolver;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.OtlpMessagingTypeResolver;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.PulsarMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.RabbitMQMessagingConsumerHandler;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.RocketMQMessagingConsumerHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceMapperTest {

    private static final byte[] TRACE_ID = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    private static final byte[] ROOT_A = {1, 1, 1, 1, 1, 1, 1, 1};
    private static final byte[] ROOT_B = {2, 2, 2, 2, 2, 2, 2, 2};
    private static final byte[] CHILD = {3, 3, 3, 3, 3, 3, 3, 3};
    private static final byte[] ORPHAN = {4, 4, 4, 4, 4, 4, 4, 4};
    private static final byte[] ABSENT_PARENT = {7, 7, 7, 7, 7, 7, 7, 7};

    private static long spanId(byte[] bytes) {
        return OtlpTraceMapperUtils.getSpanId(ByteString.copyFrom(bytes));
    }

    private static final ServiceTypeRegistryService REGISTRY = stubRegistry();

    private static ServiceTypeRegistryService stubRegistry() {
        return new ServiceTypeRegistryService() {
            @Override
            public ServiceType findServiceType(int code) {
                return ServiceType.UNDEFINED;
            }

            @Override
            public ServiceType findServiceTypeByName(String typeName) {
                return ServiceType.UNDEFINED;
            }

            @Override
            public List<ServiceType> findDesc(String desc) {
                return List.of();
            }
        };
    }

    private static OtlpTraceMapper newMapper() {
        ObjectMapper json = new ObjectMapper();
        OtlpTraceEventMapper eventMapper = new OtlpTraceEventMapper(json, 8192);
        OtlpExceptionInfoResolver exceptionInfoResolver = new OtlpExceptionInfoResolver();
        OtlpMessagingTypeResolver messagingTypeResolver = new OtlpMessagingTypeResolver(REGISTRY);
        OtlpTraceSpanMapper spanMapper = new OtlpTraceSpanMapper(
                eventMapper,
                new OtlpTraceLinkMapper(json, 8192),
                new OtlpServerTypeResolver(REGISTRY),
                exceptionInfoResolver,
                new OtlpMessagingConsumerResolver(List.of(
                        new KafkaMessagingConsumerHandler(),
                        new RabbitMQMessagingConsumerHandler(),
                        new PulsarMessagingConsumerHandler(),
                        new RocketMQMessagingConsumerHandler(),
                        new ActiveMQMessagingConsumerHandler()), messagingTypeResolver),
                new OtlpAttributeBoMapper(8192));
        OtlpTraceSpanEventMapper spanEventMapper = new OtlpTraceSpanEventMapper(
                eventMapper,
                REGISTRY,
                new OtlpMessagingTypeResolver(REGISTRY),
                new OtlpClientTypeResolver(REGISTRY),
                exceptionInfoResolver,
                new OtlpAttributeBoMapper(8192),
                8192);
        OtlpTraceSpanChunkMapper spanChunkMapper = new OtlpTraceSpanChunkMapper(spanEventMapper);
        return new OtlpTraceMapper(spanMapper, spanEventMapper, spanChunkMapper,
                new OtlpAgentInfoMapper(), new OtlpExceptionMapper(8192, 256, 2048, new SimpleMeterRegistry()),
                exceptionInfoResolver, false);
    }

    private static Span.Event exceptionEvent(String type) {
        return Span.Event.newBuilder()
                .setName("exception")
                .setTimeUnixNano(2_000_000_000L)
                .addAttributes(kv("exception.type", strVal(type)))
                .addAttributes(kv("exception.message", strVal(type + " happened")))
                .build();
    }

    private static Span serverRoot(byte[] spanId, String route, boolean withException) {
        Span.Builder builder = Span.newBuilder()
                .setName(route)
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(spanId))
                .setKindValue(Span.SpanKind.SPAN_KIND_SERVER_VALUE)
                .setStartTimeUnixNano(1_000_000_000L)
                .setEndTimeUnixNano(3_000_000_000L)
                .addAttributes(kv("http.route", strVal(route)));
        if (withException) {
            builder.addEvents(exceptionEvent("java.lang.RuntimeException"));
        }
        return builder.build();
    }

    private static Span clientChild(byte[] spanId, byte[] parentSpanId, boolean withException) {
        Span.Builder builder = Span.newBuilder()
                .setName("GET /downstream")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(spanId))
                .setParentSpanId(ByteString.copyFrom(parentSpanId))
                .setKindValue(Span.SpanKind.SPAN_KIND_CLIENT_VALUE)
                .setStartTimeUnixNano(1_500_000_000L)
                .setEndTimeUnixNano(2_500_000_000L);
        if (withException) {
            builder.addEvents(exceptionEvent("java.io.IOException"));
        }
        return builder.build();
    }

    private static List<ResourceSpans> resourceSpans(Span... spans) {
        Resource resource = Resource.newBuilder()
                .addAttributes(kv("pinpoint.applicationName", strVal("app-1")))
                .addAttributes(kv("pinpoint.agentId", strVal("agent-1")))
                .build();
        ScopeSpans.Builder scope = ScopeSpans.newBuilder();
        for (Span span : spans) {
            scope.addSpans(span);
        }
        return List.of(ResourceSpans.newBuilder()
                .setResource(resource)
                .addScopeSpans(scope)
                .build());
    }

    private static ExceptionMetaDataBo findBySpanId(OtlpTraceMapperData data, long rootSpanId) {
        return data.getExceptionMetaDataBoList().stream()
                .filter(bo -> bo.getSpanId() == rootSpanId)
                .findFirst()
                .orElse(null);
    }

    // =======================================================================
    // multi-root: each exception is attributed to its own root URI / spanId
    // =======================================================================

    @Test
    void multiRoot_eachExceptionGetsOwnRootUriAndSpanId() {
        Span rootA = serverRoot(ROOT_A, "/api/a", true);
        Span rootB = serverRoot(ROOT_B, "/api/b", true);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(rootA, rootB));

        assertThat(data.getExceptionMetaDataBoList()).hasSize(2);

        ExceptionMetaDataBo boA = findBySpanId(data, spanId(ROOT_A));
        ExceptionMetaDataBo boB = findBySpanId(data, spanId(ROOT_B));
        assertThat(boA).isNotNull();
        assertThat(boB).isNotNull();
        assertThat(boA.getUriTemplate()).isEqualTo("/api/a");
        assertThat(boB.getUriTemplate()).isEqualTo("/api/b");
    }

    // =======================================================================
    // child exception inherits root spanId + URI, keeps its own exceptionId
    // =======================================================================

    @Test
    void childException_inheritsRootSpanIdAndUri() {
        Span root = serverRoot(ROOT_A, "/api/orders", false);
        Span child = clientChild(CHILD, ROOT_A, true);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(root, child));

        assertThat(data.getExceptionMetaDataBoList()).hasSize(1);
        ExceptionMetaDataBo bo = data.getExceptionMetaDataBoList().get(0);
        // linked to the transaction root, attributed to the root URI
        assertThat(bo.getSpanId()).isEqualTo(spanId(ROOT_A));
        assertThat(bo.getUriTemplate()).isEqualTo("/api/orders");
        // but discriminated by the exception-bearing child's span id
        assertThat(bo.getExceptionWrapperBos().get(0).getExceptionId()).isEqualTo(spanId(CHILD));
    }

    @Test
    void rootAndChildExceptions_areDistinguishedByExceptionId() {
        Span root = serverRoot(ROOT_A, "/api/orders", true);
        Span child = clientChild(CHILD, ROOT_A, true);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(root, child));

        assertThat(data.getExceptionMetaDataBoList()).hasSize(2);
        // both share the root spanId
        assertThat(data.getExceptionMetaDataBoList())
                .allMatch(bo -> bo.getSpanId() == spanId(ROOT_A));
        // distinct exceptionIds: root's own id and the child's id
        assertThat(data.getExceptionMetaDataBoList())
                .map(bo -> bo.getExceptionWrapperBos().get(0).getExceptionId())
                .containsExactlyInAnyOrder(spanId(ROOT_A), spanId(CHILD));
    }

    // =======================================================================
    // orphan / root-less spans: exception not recorded (no transaction spanId)
    // =======================================================================

    @Test
    void orphanException_notRecorded_butSpanChunkStored() {
        // CLIENT span whose parent is absent from the group → no root → goes to spanChunk path.
        Span orphan = clientChild(ORPHAN, ABSENT_PARENT, true);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(orphan));

        assertThat(data.getExceptionMetaDataBoList()).isEmpty();
        assertThat(data.getSpanChunkBoList()).isNotEmpty();
    }

    // =======================================================================
    // exception-trace deep-link annotation (EXCEPTION_CHAIN_ID) — linked only
    // =======================================================================

    private static Long exceptionChainId(SpanEventBo event) {
        return event.getAnnotationBoList().stream()
                .filter(a -> a.getKey() == AnnotationKey.EXCEPTION_CHAIN_ID.getCode())
                .map(a -> (Long) a.getValue())
                .findFirst()
                .orElse(null);
    }

    // status ERROR triggers the inline exceptionInfo, which the deep-link annotation attaches to.
    private static Span withError(Span span) {
        return span.toBuilder()
                .setStatus(Status.newBuilder().setCode(Status.StatusCode.STATUS_CODE_ERROR))
                .build();
    }

    @Test
    void linkedChildException_emitsExceptionChainIdMatchingExceptionId() {
        Span root = serverRoot(ROOT_A, "/api/orders", false);
        Span child = withError(clientChild(CHILD, ROOT_A, true));

        OtlpTraceMapperData data = newMapper().map(resourceSpans(root, child));

        SpanEventBo childEvent = data.getSpanBoList().get(0).getSpanEventBoList().get(0);
        // deep-link id equals the stored exceptiontrace exceptionId (the child span id)
        assertThat(exceptionChainId(childEvent)).isEqualTo(spanId(CHILD));
        assertThat(data.getExceptionMetaDataBoList().get(0)
                .getExceptionWrapperBos().get(0).getExceptionId()).isEqualTo(spanId(CHILD));
    }

    @Test
    void orphanChunkException_doesNotEmitExceptionChainId() {
        // orphan chunk has no exceptiontrace row → must not carry a (dead) deep-link
        Span orphan = withError(clientChild(ORPHAN, ABSENT_PARENT, true));

        OtlpTraceMapperData data = newMapper().map(resourceSpans(orphan));

        SpanEventBo orphanEvent = data.getSpanChunkBoList().get(0).getSpanEventBoList().get(0);
        assertThat(exceptionChainId(orphanEvent)).isNull();
        // but the inline exception marker is still present
        assertThat(orphanEvent.hasException()).isTrue();
    }
}
