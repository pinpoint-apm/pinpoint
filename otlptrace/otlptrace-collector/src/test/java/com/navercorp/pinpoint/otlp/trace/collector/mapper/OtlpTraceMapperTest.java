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
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceRejectReason;
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
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.SpanFlags;
import io.opentelemetry.proto.trace.v1.Status;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

    static OtlpTraceMapper newMapper() {
        ObjectMapper json = new ObjectMapper();
        OtlpTraceEventMapper eventMapper = new OtlpTraceEventMapper(json, 8192);
        OtlpExceptionInfoResolver exceptionInfoResolver = new OtlpExceptionInfoResolver();
        OtlpMessagingTypeResolver messagingTypeResolver = new OtlpMessagingTypeResolver(REGISTRY);
        OtlpTraceSpanMapper spanMapper = new OtlpTraceSpanMapper(
                eventMapper,
                new OtlpTraceLinkMapper(json, 8192),
                new OtlpServerTypeResolver(REGISTRY),
                new OtlpEnvoyRecorder(),
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
                new OtlpTraceLinkMapper(json, 8192),
                REGISTRY,
                new OtlpMessagingTypeResolver(REGISTRY),
                new OtlpClientTypeResolver(REGISTRY),
                new OtlpEnvoyRecorder(),
                exceptionInfoResolver,
                new OtlpAttributeBoMapper(8192),
                8192);
        OtlpTraceSpanChunkMapper spanChunkMapper = new OtlpTraceSpanChunkMapper(spanEventMapper);
        return new OtlpTraceMapper(spanMapper, spanEventMapper, spanChunkMapper,
                new OtlpAgentInfoMapper(), new OtlpExceptionMapper(2048, 256, 2048, new SimpleMeterRegistry()),
                exceptionInfoResolver, new OtlpAgentStartTimeResolver(new SimpleMeterRegistry()), false);
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

    @Test
    void invalidId_span_isRejectedAndCounted() {
        OtlpTraceMapper mapper = newMapper();
        Span validRoot = serverRoot(ROOT_A, "/ok", false);
        // all-zero span id -> invalid; dropped at the span-map gate before it can become a storage key
        Span invalidRoot = serverRoot(new byte[8], "/bad", false);

        OtlpTraceMapperData data = mapper.map(resourceSpans(validRoot, invalidRoot));

        assertThat(data.getSpanBoList()).hasSize(1);
        assertThat(data.getRejectedSpan().count()).isEqualTo(1);
        assertThat(data.getRejectedSpan().getMessage()).contains("invalid id");
    }

    @Test
    void invalidId_span_isCountedUnderInvalidIdReason() {
        Span invalidRoot = serverRoot(new byte[8], "/bad", false);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(invalidRoot));

        assertThat(data.getRejectedSpan().count(OtlpTraceRejectReason.INVALID_ID)).isEqualTo(1);
        assertThat(data.getRejectedSpan().countByReason()).containsOnlyKeys(OtlpTraceRejectReason.INVALID_ID);
    }

    @Test
    void invalidResource_rejectsEverySpanOfTheResource_notTheScopeBlockCount() {
        // No pinpoint.applicationName / agentId and fallback disabled -> the whole ResourceSpans is
        // rejected. It carries 3 spans in 2 scope blocks: rejected_spans must be 3 (the old code
        // added getScopeSpansCount() = 2).
        ResourceSpans noIds = ResourceSpans.newBuilder()
                .setResource(Resource.newBuilder().addAttributes(kv("service.name", strVal("svc"))))
                .addScopeSpans(ScopeSpans.newBuilder()
                        .addSpans(serverRoot(ROOT_A, "/a", false))
                        .addSpans(serverRoot(ROOT_B, "/b", false)))
                .addScopeSpans(ScopeSpans.newBuilder()
                        .addSpans(serverRoot(CHILD, "/c", false)))
                .build();

        OtlpTraceMapperData data = newMapper().map(List.of(noIds));

        assertThat(data.getSpanBoList()).isEmpty();
        assertThat(data.getRejectedSpan().count()).isEqualTo(3);
        assertThat(data.getRejectedSpan().count(OtlpTraceRejectReason.INVALID_RESOURCE)).isEqualTo(3);
        assertThat(data.getRejectedSpan().getMessage()).endsWith("(3)");
    }

    @Test
    void invalidParentSpanId_child_isRejected() {
        OtlpTraceMapper mapper = newMapper();
        Span root = serverRoot(ROOT_A, "/ok", false);
        // present-but-all-zero parentSpanId -> invalid; the child span is rejected (decision A)
        Span badChild = clientChild(CHILD, new byte[8], false);

        OtlpTraceMapperData data = mapper.map(resourceSpans(root, badChild));

        assertThat(data.getSpanBoList()).hasSize(1);
        assertThat(data.getRejectedSpan().count()).isEqualTo(1);
        assertThat(data.getRejectedSpan().getMessage()).contains("invalid id");
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
    void unroutedRootException_uriTemplateIsEmpty_notRawPath() {
        // No http.route: exceptions of an unrouted request store an empty uriTemplate (agent parity),
        // not the raw url.path — one group instead of one per requested path.
        Span root = Span.newBuilder(serverRoot(ROOT_A, "GET", true))
                .clearAttributes()
                .addAttributes(kv("url.path", strVal("/nosuchpath/123")))
                .build();

        OtlpTraceMapperData data = newMapper().map(resourceSpans(root));

        assertThat(data.getExceptionMetaDataBoList()).hasSize(1);
        assertThat(data.getExceptionMetaDataBoList().get(0).getUriTemplate()).isEmpty();
        // the transaction rpc still carries the raw path
        assertThat(data.getSpanBoList().get(0).getRpc()).isEqualTo("/nosuchpath/123");
    }

    @Test
    void childException_underUnroutedRoot_inheritsEmptyUriTemplate() {
        // A downstream CLIENT exception under an unrouted root is attributed to the root's (empty)
        // template, not to the root's raw path and not to the child's own span name.
        Span root = Span.newBuilder(serverRoot(ROOT_A, "GET", false))
                .clearAttributes()
                .addAttributes(kv("url.path", strVal("/nosuchpath/123")))
                .build();
        Span child = clientChild(CHILD, ROOT_A, true);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(root, child));

        assertThat(data.getExceptionMetaDataBoList()).hasSize(1);
        ExceptionMetaDataBo bo = data.getExceptionMetaDataBoList().get(0);
        assertThat(bo.getUriTemplate()).isEmpty();
        assertThat(bo.getSpanId()).isEqualTo(spanId(ROOT_A));
    }

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

    // =======================================================================
    // instrumentation scope → OPENTELEMETRY_SCOPE annotation (end-to-end)
    // =======================================================================

    private static List<ResourceSpans> resourceSpansWithScope(InstrumentationScope scope, Span... spans) {
        Resource resource = Resource.newBuilder()
                .addAttributes(kv("pinpoint.applicationName", strVal("app-1")))
                .addAttributes(kv("pinpoint.agentId", strVal("agent-1")))
                .build();
        ScopeSpans.Builder scopeSpans = ScopeSpans.newBuilder().setScope(scope);
        for (Span span : spans) {
            scopeSpans.addSpans(span);
        }
        return List.of(ResourceSpans.newBuilder()
                .setResource(resource)
                .addScopeSpans(scopeSpans)
                .build());
    }

    private static Object scopeAnnotation(List<AnnotationBo> annotations) {
        return annotations.stream()
                .filter(a -> a.getKey() == AnnotationKey.OPENTELEMETRY_SCOPE.getCode())
                .map(AnnotationBo::getValue)
                .findFirst()
                .orElse(null);
    }

    private static Span consumerSpan(byte[] spanId, byte[] parentSpanId, long startTimeNanos) {
        Span.Builder builder = Span.newBuilder()
                .setName("orders process")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(spanId))
                .setKindValue(Span.SpanKind.SPAN_KIND_CONSUMER_VALUE)
                .setStartTimeUnixNano(startTimeNanos)
                .setEndTimeUnixNano(startTimeNanos + 1_000_000_000L);
        if (parentSpanId != null) {
            builder.setParentSpanId(ByteString.copyFrom(parentSpanId));
        }
        return builder.build();
    }

    @Test
    void scope_propagatesToRootSpanAndChildSpanEvent() {
        InstrumentationScope scope = InstrumentationScope.newBuilder()
                .setName("io.opentelemetry.spring-webmvc-6.0")
                .setVersion("2.5.0")
                .build();
        Span root = serverRoot(ROOT_A, "/api/orders", false);
        Span child = clientChild(CHILD, ROOT_A, false);

        OtlpTraceMapperData data = newMapper().map(resourceSpansWithScope(scope, root, child));

        SpanBo spanBo = data.getSpanBoList().get(0);
        assertThat(scopeAnnotation(spanBo.getAnnotationBoList()))
                .isEqualTo("io.opentelemetry.spring-webmvc-6.0@2.5.0");
        SpanEventBo childEvent = spanBo.getSpanEventBoList().get(0);
        assertThat(scopeAnnotation(childEvent.getAnnotationBoList()))
                .isEqualTo("io.opentelemetry.spring-webmvc-6.0@2.5.0");
    }

    @Test
    void scope_unset_noScopeAnnotation() {
        // resourceSpans() builds ScopeSpans without a scope (proto default, empty name) —
        // the pre-scope wire shape; no OPENTELEMETRY_SCOPE annotation may appear.
        Span root = serverRoot(ROOT_A, "/api/orders", false);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(root));

        assertThat(scopeAnnotation(data.getSpanBoList().get(0).getAnnotationBoList())).isNull();
    }

    @Test
    void scope_propagatesToOrphanSpanChunk() {
        InstrumentationScope scope = InstrumentationScope.newBuilder()
                .setName("io.opentelemetry.okhttp-3.0")
                .build();
        Span orphan = clientChild(ORPHAN, ABSENT_PARENT, false);

        OtlpTraceMapperData data = newMapper().map(resourceSpansWithScope(scope, orphan));

        SpanEventBo orphanEvent = data.getSpanChunkBoList().get(0).getSpanEventBoList().get(0);
        assertThat(scopeAnnotation(orphanEvent.getAnnotationBoList()))
                .isEqualTo("io.opentelemetry.okhttp-3.0");
    }

    @Test
    void scope_survivesWrapperRootRealignment() {
        // CONSUMER→CONSUMER wrapper: alignWrapperRoots rebuilds the wrapper span
        // (start realigned, kind→SERVER); the rebuilt ScopedSpan must retain its scope.
        InstrumentationScope scope = InstrumentationScope.newBuilder()
                .setName("io.opentelemetry.kafka-clients-2.6")
                .build();
        Span wrapper = consumerSpan(ROOT_A, null, 2_000_000_000L);      // late start → realigned
        Span inner = consumerSpan(CHILD, ROOT_A, 1_000_000_000L);

        OtlpTraceMapperData data = newMapper().map(resourceSpansWithScope(scope, wrapper, inner));

        assertThat(data.getSpanBoList()).isNotEmpty();
        for (SpanBo spanBo : data.getSpanBoList()) {
            assertThat(scopeAnnotation(spanBo.getAnnotationBoList()))
                    .isEqualTo("io.opentelemetry.kafka-clients-2.6");
        }
    }

    // =======================================================================
    // Span.Link on child spans → OPENTELEMETRY_LINK annotation (end-to-end)
    // =======================================================================

    private static final byte[] LINK_TRACE_ID = {9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9};
    private static final byte[] LINK_SPAN_ID = {8, 7, 6, 5, 4, 3, 2, 1};

    private static Object linkAnnotation(List<AnnotationBo> annotations) {
        return annotations.stream()
                .filter(a -> a.getKey() == AnnotationKey.OPENTELEMETRY_LINK.getCode())
                .map(AnnotationBo::getValue)
                .findFirst()
                .orElse(null);
    }

    // kind=0 span shape as exported by non-SDK tracers (e.g. Claude Code): every span is
    // SPAN_KIND_UNSPECIFIED and cross-trace links ride on child spans, never on the root.
    private static Span unspecifiedSpan(byte[] spanId, byte[] parentSpanId, Span.Link link) {
        Span.Builder builder = Span.newBuilder()
                .setName("claude_code.op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(spanId))
                .setKindValue(Span.SpanKind.SPAN_KIND_UNSPECIFIED_VALUE)
                .setStartTimeUnixNano(1_000_000_000L)
                .setEndTimeUnixNano(2_000_000_000L);
        if (parentSpanId != null) {
            builder.setParentSpanId(ByteString.copyFrom(parentSpanId));
        }
        if (link != null) {
            builder.addLinks(link);
        }
        return builder.build();
    }

    private static Span.Link crossTraceLink() {
        return Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(LINK_TRACE_ID))
                .setSpanId(ByteString.copyFrom(LINK_SPAN_ID))
                .addAttributes(kv("link.type", strVal("parent_of")))
                .build();
    }

    @Test
    void childLink_survivesOnRootLinkedSpanEvent() {
        Span root = unspecifiedSpan(ROOT_A, null, null);
        Span child = unspecifiedSpan(CHILD, ROOT_A, crossTraceLink());

        OtlpTraceMapperData data = newMapper().map(resourceSpans(root, child));

        assertThat(data.getSpanBoList()).hasSize(1);
        SpanEventBo childEvent = data.getSpanBoList().get(0).getSpanEventBoList().get(0);
        Object value = linkAnnotation(childEvent.getAnnotationBoList());
        assertThat(value).isNotNull();
        assertThat((String) value).contains("09090909090909090909090909090909");
    }

    @Test
    void childLink_survivesOnOrphanSpanChunk() {
        // Split arrival: the link-bearing child lands in a batch without its root and is
        // stored as an orphan SpanChunk — the link must survive on that path too.
        Span orphan = unspecifiedSpan(ORPHAN, ABSENT_PARENT, crossTraceLink());

        OtlpTraceMapperData data = newMapper().map(resourceSpans(orphan));

        SpanEventBo orphanEvent = data.getSpanChunkBoList().get(0).getSpanEventBoList().get(0);
        assertThat(linkAnnotation(orphanEvent.getAnnotationBoList())).isNotNull();
    }

    // =======================================================================
    // sampled flag — explicitly unsampled spans are dropped and counted as rejected
    // =======================================================================

    private static Span withFlags(Span span, int flags) {
        return span.toBuilder().setFlags(flags).build();
    }

    // A definitively-unsampled wire shape: the trace-flags byte is populated (W3C "random" bit,
    // 0x02) so we know the sampling decision is real, while the sampled bit (0x01) stays clear.
    // is_remote metadata is included to mirror a modern SDK.
    private static final int UNSAMPLED_FLAGS =
            SpanFlags.SPAN_FLAGS_CONTEXT_HAS_IS_REMOTE_MASK_VALUE | 0x02;

    @Test
    void isUnsampled_flagRules() {
        Span base = serverRoot(ROOT_A, "/api/orders", false);
        // flags unset (pre-1.1 exporter): ambiguous — keep
        assertThat(OtlpTraceMapper.isUnsampled(withFlags(base, 0))).isFalse();
        // sampled bit set, remote bits unset
        assertThat(OtlpTraceMapper.isUnsampled(withFlags(base, 0x01))).isFalse();
        // is_remote metadata only: the trace-flags byte is unpopulated — NOT a sampling signal,
        // so keep (e.g. a locust load-generator root span exported with is_remote but no sampled bit).
        assertThat(OtlpTraceMapper.isUnsampled(
                withFlags(base, SpanFlags.SPAN_FLAGS_CONTEXT_HAS_IS_REMOTE_MASK_VALUE))).isFalse();
        // is_remote metadata + sampled bit set
        assertThat(OtlpTraceMapper.isUnsampled(
                withFlags(base, SpanFlags.SPAN_FLAGS_CONTEXT_HAS_IS_REMOTE_MASK_VALUE | 0x01))).isFalse();
        // trace-flags byte populated (random bit) with the sampled bit clear: definitively unsampled
        assertThat(OtlpTraceMapper.isUnsampled(withFlags(base, UNSAMPLED_FLAGS))).isTrue();
    }

    @Test
    void unsampledSpan_droppedAndCountedAsRejected() {
        Span unsampledRoot = withFlags(serverRoot(ROOT_A, "/api/orders", false), UNSAMPLED_FLAGS);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(unsampledRoot));

        assertThat(data.getSpanBoList()).isEmpty();
        assertThat(data.getRejectedSpan().count()).isEqualTo(1);
        assertThat(data.getRejectedSpan().getMessage()).contains("unsampled span (1)");
    }

    @Test
    void sampledSpan_kept() {
        Span sampledRoot = withFlags(serverRoot(ROOT_A, "/api/orders", false),
                SpanFlags.SPAN_FLAGS_CONTEXT_HAS_IS_REMOTE_MASK_VALUE | 0x01);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(sampledRoot));

        assertThat(data.getSpanBoList()).hasSize(1);
        assertThat(data.getRejectedSpan().count()).isZero();
    }

    @Test
    void isRemoteMetadataOnly_keptConservatively() {
        // Regression: a span carrying only is_remote metadata (trace-flags byte all-clear) must be
        // kept. Reading the non-zero flags int as "populated" previously dropped these — which
        // silently lost a whole service's traces (locust load-generator root spans).
        Span isRemoteOnlyRoot = withFlags(serverRoot(ROOT_A, "/api/orders", false),
                SpanFlags.SPAN_FLAGS_CONTEXT_HAS_IS_REMOTE_MASK_VALUE);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(isRemoteOnlyRoot));

        assertThat(data.getSpanBoList()).hasSize(1);
        assertThat(data.getRejectedSpan().count()).isZero();
    }

    @Test
    void zeroFlags_treatedAsLegacyAndKept() {
        // serverRoot() never sets flags — the pre-flags wire shape must keep flowing unchanged
        Span legacyRoot = serverRoot(ROOT_A, "/api/orders", false);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(legacyRoot));

        assertThat(data.getSpanBoList()).hasSize(1);
        assertThat(data.getRejectedSpan().count()).isZero();
    }

    @Test
    void unsampledSpan_isCountedUnderUnsampledReason() {
        Span unsampledRoot = withFlags(serverRoot(ROOT_A, "/api/orders", false), UNSAMPLED_FLAGS);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(unsampledRoot));

        assertThat(data.getRejectedSpan().count(OtlpTraceRejectReason.UNSAMPLED)).isEqualTo(1);
        assertThat(data.getRejectedSpan().count(OtlpTraceRejectReason.INVALID_ID)).isZero();
    }

    @Test
    void unsampledChild_droppedButSampledRootKept() {
        // per-span drop: the sampled root survives; the explicitly unsampled child is rejected
        Span root = serverRoot(ROOT_A, "/api/orders", false);
        Span unsampledChild = withFlags(clientChild(CHILD, ROOT_A, false), UNSAMPLED_FLAGS);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(root, unsampledChild));

        assertThat(data.getSpanBoList()).hasSize(1);
        assertThat(data.getSpanBoList().get(0).getSpanEventBoList()).isEmpty();
        assertThat(data.getRejectedSpan().count()).isEqualTo(1);
    }

    // =======================================================================
    // agentStartTime — process.creation.time resource attribute
    // =======================================================================

    private static List<ResourceSpans> resourceSpansWithCreationTime(String creationTime, Span... spans) {
        Resource resource = Resource.newBuilder()
                .addAttributes(kv("pinpoint.applicationName", strVal("app-1")))
                .addAttributes(kv("pinpoint.agentId", strVal("agent-1")))
                .addAttributes(kv("process.creation.time", strVal(creationTime)))
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

    @Test
    void agentStartTime_fromProcessCreationTime() {
        // 2026-07-01T00:00:00Z = 1782864000000 epoch millis
        Span rootA = serverRoot(ROOT_A, "/api/orders", false);
        Span rootB = serverRoot(ROOT_B, "/api/items", false);

        OtlpTraceMapperData data = newMapper().map(
                resourceSpansWithCreationTime("2026-07-01T00:00:00Z", rootA, rootB));

        assertThat(data.getSpanBoList()).hasSize(2);
        // every root span of the ResourceSpans carries the same process session time
        for (SpanBo spanBo : data.getSpanBoList()) {
            assertThat(spanBo.getSpanOwner().getAgentStartTime()).isEqualTo(1782864000000L);
        }
        // AgentInfoBo (AGENTINFO rowkey) uses the same value
        assertThat(data.getAgentInfoBoList()).isNotEmpty();
        assertThat(data.getAgentInfoBoList().get(0).getStartTime()).isEqualTo(1782864000000L);
    }

    @Test
    void agentStartTime_absentCreationTime_keepsSpanStartTime() {
        // regression: without process.creation.time the pre-existing approximation
        // (span start time) must remain unchanged. serverRoot starts at 1_000_000_000ns = 1000ms.
        Span root = serverRoot(ROOT_A, "/api/orders", false);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(root));

        assertThat(data.getSpanBoList()).hasSize(1);
        assertThat(data.getSpanBoList().get(0).getSpanOwner().getAgentStartTime()).isEqualTo(1000L);
    }

    @Test
    void agentStartTime_invalidCreationTime_fallsBackToSpanStartTime() {
        Span root = serverRoot(ROOT_A, "/api/orders", false);

        OtlpTraceMapperData data = newMapper().map(
                resourceSpansWithCreationTime("not-a-timestamp", root));

        assertThat(data.getSpanBoList()).hasSize(1);
        assertThat(data.getSpanBoList().get(0).getSpanOwner().getAgentStartTime()).isEqualTo(1000L);
    }

    // =======================================================================
    // URI stat collection — entry-point spans keyed by route template, or /NULL when unrouted HTTP
    // =======================================================================

    private static OtlpUriStatSpan uriStatSpanOf(OtlpTraceMapperData data, String uri) {
        return data.getUriStatSpanList().stream()
                .filter(s -> s.getUri().equals(uri))
                .findFirst()
                .orElse(null);
    }

    @Test
    void uriStat_rootWithRoute_collectedWithSpanFields() {
        Span okRoot = serverRoot(ROOT_A, "/api/orders", false);
        Span errorRoot = withError(serverRoot(ROOT_B, "/api/items", false));

        OtlpTraceMapperData data = newMapper().map(resourceSpans(okRoot, errorRoot));

        assertThat(data.getUriStatSpanList()).hasSize(2);
        OtlpUriStatSpan ok = uriStatSpanOf(data, "/api/orders");
        assertThat(ok).isNotNull();
        assertThat(ok.getApplicationName()).isEqualTo("app-1");
        assertThat(ok.getAgentId()).isEqualTo("agent-1");
        // serverRoot: start 1_000_000_000ns = 1000ms, end 3_000_000_000ns → elapsed 2000ms
        assertThat(ok.getStartTime()).isEqualTo(1000L);
        assertThat(ok.getElapsed()).isEqualTo(2000);
        assertThat(ok.isError()).isFalse();
        // status ERROR → SpanBo errCode != 0 → feeds the failure histogram
        OtlpUriStatSpan error = uriStatSpanOf(data, "/api/items");
        assertThat(error).isNotNull();
        assertThat(error.isError()).isTrue();
    }

    @Test
    void uriStat_rootWithNextRoute_collected() {
        // A Next.js entry point routed via next.route (no http.route) contributes like an http.route one.
        Span root = Span.newBuilder(serverRoot(ROOT_A, "GET", false))
                .clearAttributes()
                .addAttributes(kv("next.route", strVal("/api/products/[productId]/index")))
                .addAttributes(kv("url.path", strVal("/api/products/0PUK6V6EV0")))
                .build();

        OtlpTraceMapperData data = newMapper().map(resourceSpans(root));

        assertThat(data.getUriStatSpanList()).hasSize(1);
        assertThat(data.getUriStatSpanList().get(0).getUri()).isEqualTo("/api/products/[productId]/index");
    }

    @Test
    void uriStat_unroutedHttpRoot_collectedUnderNullUri() {
        // Unrouted HTTP SERVER root (url.path but no template): the trace is stored and the request
        // is counted under the "/NULL" bucket like the agent does — never under the raw path
        // (low-cardinality contract). The error flag still feeds the failure histogram.
        Span okRoot = Span.newBuilder(serverRoot(ROOT_A, "GET", false))
                .clearAttributes()
                .addAttributes(kv("http.request.method", strVal("GET")))
                .addAttributes(kv("url.path", strVal("/nosuchpath/123")))
                .build();
        Span errorRoot = withError(Span.newBuilder(serverRoot(ROOT_B, "GET", false))
                .clearAttributes()
                .addAttributes(kv("http.url", strVal("http://frontend:8080/nosuchpath/456")))
                .build());

        OtlpTraceMapperData data = newMapper().map(resourceSpans(okRoot, errorRoot));

        assertThat(data.getSpanBoList()).hasSize(2);
        assertThat(data.getUriStatSpanList())
                .extracting(OtlpUriStatSpan::getUri)
                .containsExactly(OtlpTraceConstants.URI_STAT_NULL_URI, OtlpTraceConstants.URI_STAT_NULL_URI);
        assertThat(data.getUriStatSpanList())
                .extracting(OtlpUriStatSpan::isError)
                .containsExactly(false, true);
        // The stored transaction keeps the raw path (drill-down), only the uriStat key is bucketed.
        assertThat(data.getSpanBoList()).extracting(SpanBo::getRpc).contains("/nosuchpath/123");
    }

    @Test
    void uriStat_serverRootWrappingRoutedServerRoot_countsInnerOnly() {
        // Node instrumentation-http (outer, raw url only) wraps the Next.js handleRequest span (inner,
        // http.route) for one request. Both stay roots for the trace store, but URI stat counts the
        // request once, by the inner span's template — not a second time as "/NULL".
        Span outer = Span.newBuilder(serverRoot(ROOT_A, "GET", false))
                .clearAttributes()
                .addAttributes(kv("http.method", strVal("POST")))
                .addAttributes(kv("http.url", strVal("http://frontend-proxy:8080/api/cart")))
                .build();
        Span inner = Span.newBuilder(serverRoot(ROOT_B, "/api/cart", false))
                .setParentSpanId(ByteString.copyFrom(ROOT_A))
                .build();

        OtlpTraceMapperData data = newMapper().map(resourceSpans(outer, inner));

        assertThat(data.getSpanBoList()).hasSize(2);
        assertThat(data.getUriStatSpanList())
                .extracting(OtlpUriStatSpan::getUri)
                .containsExactly("/api/cart");
    }

    @Test
    void uriStat_serverRootWrappingRoutedServerRoot_bothRouted_countsOnce() {
        // Next.js 16.2+ propagates http.route to the parent span as well: without the wrapper rule
        // the same request would be counted twice under the same template.
        Span outer = serverRoot(ROOT_A, "/api/cart", false);
        Span inner = Span.newBuilder(serverRoot(ROOT_B, "/api/cart", false))
                .setParentSpanId(ByteString.copyFrom(ROOT_A))
                .build();

        OtlpTraceMapperData data = newMapper().map(resourceSpans(outer, inner));

        assertThat(data.getUriStatSpanList())
                .extracting(OtlpUriStatSpan::getUri)
                .containsExactly("/api/cart");
    }

    @Test
    void uriStat_wrappedServerRootArrivingAlone_isCounted() {
        // The wrapper rule only sees the spans of the current ResourceSpans. When the SDK exports the
        // inner span in a later batch, the outer span is an ordinary unrouted SERVER root here and is
        // counted under "/NULL" — an over-count in that batch, never a lost request.
        Span outer = Span.newBuilder(serverRoot(ROOT_A, "GET", false))
                .clearAttributes()
                .addAttributes(kv("http.url", strVal("http://frontend-proxy:8080/api/cart")))
                .build();

        OtlpTraceMapperData data = newMapper().map(resourceSpans(outer));

        assertThat(data.getUriStatSpanList())
                .extracting(OtlpUriStatSpan::getUri)
                .containsExactly(OtlpTraceConstants.URI_STAT_NULL_URI);
    }

    @Test
    void uriStat_serverRootWithNonServerChildRoot_stillCounted() {
        // Only a SERVER child demotes the wrapper. A CONSUMER child root (message handled inside an
        // HTTP request) leaves the SERVER root as the URI stat entry point.
        Span outer = serverRoot(ROOT_A, "/api/orders", false);
        Span consumerChild = Span.newBuilder(serverRoot(ROOT_B, "orders process", false))
                .clearAttributes()
                .setKindValue(Span.SpanKind.SPAN_KIND_CONSUMER_VALUE)
                .setParentSpanId(ByteString.copyFrom(ROOT_A))
                .build();

        OtlpTraceMapperData data = newMapper().map(resourceSpans(outer, consumerChild));

        assertThat(data.getUriStatSpanList())
                .extracting(OtlpUriStatSpan::getUri)
                .containsExactly("/api/orders");
    }

    @Test
    void findServerWrapperRoots_returnsOnlyServerParentsOfServerRoots() {
        Span a = serverRoot(ROOT_A, "/a", false);
        Span b = Span.newBuilder(serverRoot(ROOT_B, "/b", false))
                .setParentSpanId(ByteString.copyFrom(ROOT_A))
                .build();
        Span c = serverRoot(CHILD, "/c", false); // unrelated SERVER root
        Span internal = Span.newBuilder(serverRoot(new byte[]{4, 4, 4, 4, 4, 4, 4, 4}, "x", false))
                .setKindValue(Span.SpanKind.SPAN_KIND_INTERNAL_VALUE)
                .setParentSpanId(ByteString.copyFrom(CHILD))
                .build(); // INTERNAL child does not make c a wrapper
        List<ScopedSpan> roots = new ArrayList<>();
        for (Span s : List.of(a, b, c, internal)) {
            roots.add(new ScopedSpan(s, InstrumentationScope.getDefaultInstance()));
        }

        assertThat(OtlpTraceMapper.findServerWrapperRoots(roots))
                .containsExactly(ByteString.copyFrom(ROOT_A));
        assertThat(OtlpTraceMapper.findServerWrapperRoots(List.of(roots.get(0)))).isEmpty();
    }

    @Test
    void uriStat_nonHttpRootWithoutRoute_notCollected() {
        // A SERVER root that is neither routed nor an HTTP request (no http.*/url.* key at all,
        // e.g. gRPC or a bare custom span): the trace is stored, but nothing is counted — "/NULL"
        // is an HTTP bucket, not a catch-all.
        Span bareRoot = serverRoot(ROOT_A, "/api/orders", false).toBuilder()
                .clearAttributes()
                .build();
        Span grpcRoot = serverRoot(ROOT_B, "GetCart", false).toBuilder()
                .clearAttributes()
                .addAttributes(kv("rpc.system", strVal("grpc")))
                .addAttributes(kv("rpc.method", strVal("GetCart")))
                .build();

        OtlpTraceMapperData data = newMapper().map(resourceSpans(bareRoot, grpcRoot));

        assertThat(data.getSpanBoList()).hasSize(2);
        assertThat(data.getUriStatSpanList()).isEmpty();
    }

    @Test
    void uriStat_childSpanWithRoute_notCollected() {
        // Only entry-point (root) spans feed URI stat. An INTERNAL child carrying http.route is
        // kind-eligible for the template, but as a child it lands in the root's span events and
        // must not add a second uriStat record.
        Span root = serverRoot(ROOT_A, "/root", false);
        Span internalChild = clientChild(CHILD, ROOT_A, false).toBuilder()
                .setKindValue(Span.SpanKind.SPAN_KIND_INTERNAL_VALUE)
                .addAttributes(kv("http.route", strVal("/child")))
                .build();

        OtlpTraceMapperData data = newMapper().map(resourceSpans(root, internalChild));

        assertThat(data.getSpanBoList()).hasSize(1);
        assertThat(data.getUriStatSpanList())
                .extracting(OtlpUriStatSpan::getUri)
                .containsExactly("/root");
    }

    // =======================================================================
    // reject reason: orphan (unlinkable spans)
    // =======================================================================

    @Test
    void unlinkableSpans_areCountedUnderOrphanReason() {
        // A parentless CLIENT span becomes a local root and is stored as a spanChunk, while a
        // two-span parent cycle (X <-> Y) hangs off no root at all: findLinkSpanChunk leaves it in
        // the child list and the mapper drops it under the orphan reason (not mapping_error).
        byte[] cycleX = {5, 5, 5, 5, 5, 5, 5, 5};
        byte[] cycleY = {6, 6, 6, 6, 6, 6, 6, 6};
        Span localRoot = clientChild(ORPHAN, ABSENT_PARENT, false);
        Span x = clientChild(cycleX, cycleY, false);
        Span y = clientChild(cycleY, cycleX, false);

        OtlpTraceMapperData data = newMapper().map(resourceSpans(localRoot, x, y));

        assertThat(data.getSpanBoList()).isEmpty();
        assertThat(data.getSpanChunkBoList()).hasSize(1);
        assertThat(data.getRejectedSpan().count()).isEqualTo(2);
        assertThat(data.getRejectedSpan().count(OtlpTraceRejectReason.ORPHAN)).isEqualTo(2);
        assertThat(data.getRejectedSpan().countByReason()).containsOnlyKeys(OtlpTraceRejectReason.ORPHAN);
        assertThat(data.getRejectedSpan().getMessage()).contains(OtlpTraceRejectReason.ORPHAN.message() + " (2)");
    }
}
