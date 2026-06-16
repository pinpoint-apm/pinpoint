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

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionWrapperBo;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpExceptionMapperTest {

    private static final byte[] TRACE_ID = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    private static final byte[] EXCEPTION_SPAN_ID = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final byte[] ROOT_SPAN_ID = {9, 9, 9, 9, 9, 9, 9, 9};

    private static final long EXCEPTION_SPAN_ID_LONG = OtlpTraceMapperUtils.getSpanId(ByteString.copyFrom(EXCEPTION_SPAN_ID));
    private static final long ROOT_SPAN_ID_LONG = OtlpTraceMapperUtils.getSpanId(ByteString.copyFrom(ROOT_SPAN_ID));

    private final OtlpExceptionMapper mapper = new OtlpExceptionMapper();

    private static IdAndName id() {
        return new IdAndName("agent-1", "agent-1", "app-1", "default");
    }

    private static Span.Builder spanBuilder(byte[] spanId) {
        return Span.newBuilder()
                .setName("op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(spanId))
                .setKindValue(Span.SpanKind.SPAN_KIND_SERVER_VALUE);
    }

    private static Span.Event exceptionEvent(KeyValue... attrs) {
        Span.Event.Builder event = Span.Event.newBuilder()
                .setName("exception")
                .setTimeUnixNano(2_000_000_000L); // 2000 ms
        for (KeyValue attr : attrs) {
            event.addAttributes(attr);
        }
        return event.build();
    }

    private static KeyValue exceptionType(String type) {
        return kv("exception.type", strVal(type));
    }

    // =======================================================================
    // Variant A: recording is independent of span status
    // =======================================================================

    @Test
    void map_recordsWhenStatusUnset() {
        // recordException and setStatus(ERROR) are independent in OTel; an exception event with
        // a resolvable type must be recorded even when the span status was never set to ERROR.
        Span span = spanBuilder(EXCEPTION_SPAN_ID)
                .setStatus(Status.newBuilder().setCode(Status.StatusCode.STATUS_CODE_UNSET))
                .addEvents(exceptionEvent(exceptionType("java.lang.RuntimeException"),
                        kv("exception.message", strVal("boom"))))
                .build();

        Optional<ExceptionMetaDataBo> result = mapper.map(id(), span, ROOT_SPAN_ID_LONG, "/api/orders");

        assertThat(result).isPresent();
    }

    @Test
    void map_recordsWhenStatusError() {
        Span span = spanBuilder(EXCEPTION_SPAN_ID)
                .setStatus(Status.newBuilder().setCode(Status.StatusCode.STATUS_CODE_ERROR))
                .addEvents(exceptionEvent(exceptionType("java.lang.RuntimeException")))
                .build();

        assertThat(mapper.map(id(), span, ROOT_SPAN_ID_LONG, "/api/orders")).isPresent();
    }

    // =======================================================================
    // spanId / exceptionId semantics
    // =======================================================================

    @Test
    void map_metaDataSpanId_isRootSpanId() {
        Span span = spanBuilder(EXCEPTION_SPAN_ID)
                .addEvents(exceptionEvent(exceptionType("java.lang.RuntimeException")))
                .build();

        ExceptionMetaDataBo bo = mapper.map(id(), span, ROOT_SPAN_ID_LONG, "/api/orders").orElseThrow();

        // ExceptionMetaDataBo.spanId must be the transaction root span id (not the exception span)
        // so the exception links back to the stored root SpanBo via (transactionId, spanId).
        assertThat(bo.getSpanId()).isEqualTo(ROOT_SPAN_ID_LONG);
        assertThat(bo.getSpanId()).isNotEqualTo(EXCEPTION_SPAN_ID_LONG);
    }

    @Test
    void map_wrapperExceptionId_isExceptionSpanId() {
        Span span = spanBuilder(EXCEPTION_SPAN_ID)
                .addEvents(exceptionEvent(exceptionType("java.lang.RuntimeException")))
                .build();

        ExceptionMetaDataBo bo = mapper.map(id(), span, ROOT_SPAN_ID_LONG, "/api/orders").orElseThrow();
        ExceptionWrapperBo wrapper = bo.getExceptionWrapperBos().get(0);

        // exceptionId discriminates multiple exceptions in one transaction (which share rootSpanId).
        assertThat(wrapper.getExceptionId()).isEqualTo(EXCEPTION_SPAN_ID_LONG);
        assertThat(wrapper.getExceptionDepth()).isEqualTo(0);
    }

    @Test
    void map_rootSpanException_spanIdEqualsExceptionId() {
        // When the exception is on the root span itself, exceptionSpanId == rootSpanId.
        Span span = spanBuilder(ROOT_SPAN_ID)
                .addEvents(exceptionEvent(exceptionType("java.lang.RuntimeException")))
                .build();

        ExceptionMetaDataBo bo = mapper.map(id(), span, ROOT_SPAN_ID_LONG, "/api/orders").orElseThrow();

        assertThat(bo.getSpanId()).isEqualTo(ROOT_SPAN_ID_LONG);
        assertThat(bo.getExceptionWrapperBos().get(0).getExceptionId()).isEqualTo(ROOT_SPAN_ID_LONG);
    }

    // =======================================================================
    // type resolution / gating
    // =======================================================================

    @Test
    void map_typeAndMessageMapped() {
        Span span = spanBuilder(EXCEPTION_SPAN_ID)
                .addEvents(exceptionEvent(exceptionType("java.io.IOException"),
                        kv("exception.message", strVal("disk full"))))
                .build();

        ExceptionWrapperBo wrapper = mapper.map(id(), span, ROOT_SPAN_ID_LONG, "/api/orders")
                .orElseThrow().getExceptionWrapperBos().get(0);

        assertThat(wrapper.getExceptionClassName()).isEqualTo("java.io.IOException");
        assertThat(wrapper.getExceptionMessage()).isEqualTo("disk full");
    }

    @Test
    void map_fallsBackToSpanErrorType_whenEventTypeMissing() {
        // exception event present but without exception.type → fall back to span attribute error.type.
        Span span = spanBuilder(EXCEPTION_SPAN_ID)
                .addAttributes(kv("error.type", strVal("java.net.SocketTimeoutException")))
                .addEvents(exceptionEvent(kv("exception.message", strVal("timeout"))))
                .build();

        ExceptionWrapperBo wrapper = mapper.map(id(), span, ROOT_SPAN_ID_LONG, "/api/orders")
                .orElseThrow().getExceptionWrapperBos().get(0);

        assertThat(wrapper.getExceptionClassName()).isEqualTo("java.net.SocketTimeoutException");
    }

    @Test
    void map_emptyWhenNoExceptionEvent() {
        // status=ERROR alone (no exception event) is not enough to record an exception trace.
        Span span = spanBuilder(EXCEPTION_SPAN_ID)
                .setStatus(Status.newBuilder().setCode(Status.StatusCode.STATUS_CODE_ERROR))
                .build();

        assertThat(mapper.map(id(), span, ROOT_SPAN_ID_LONG, "/api/orders")).isEmpty();
    }

    @Test
    void map_emptyWhenNoTypeResolvable() {
        // exception event present but neither exception.type nor error.type available.
        Span span = spanBuilder(EXCEPTION_SPAN_ID)
                .addEvents(exceptionEvent(kv("exception.message", strVal("no type here"))))
                .build();

        assertThat(mapper.map(id(), span, ROOT_SPAN_ID_LONG, "/api/orders")).isEmpty();
    }

    // =======================================================================
    // full stacktrace preserved (not truncated)
    // =======================================================================

    @Test
    void map_keepsParsedStackTrace() {
        String stackTrace = "java.lang.RuntimeException: boom\n"
                + "\tat com.example.Service.handle(Service.java:42)\n"
                + "\tat com.example.Controller.get(Controller.java:17)\n";
        Span span = spanBuilder(EXCEPTION_SPAN_ID)
                .addEvents(exceptionEvent(exceptionType("java.lang.RuntimeException"),
                        kv("exception.stacktrace", strVal(stackTrace))))
                .build();

        ExceptionWrapperBo wrapper = mapper.map(id(), span, ROOT_SPAN_ID_LONG, "/api/orders")
                .orElseThrow().getExceptionWrapperBos().get(0);

        assertThat(wrapper.getStackTraceElements()).hasSize(2);
        assertThat(wrapper.getStackTraceElements().get(0).getClassName()).isEqualTo("com.example.Service");
        assertThat(wrapper.getStackTraceElements().get(0).getMethodName()).isEqualTo("handle");
    }

    @Test
    void map_uriTemplatePassedThrough() {
        Span span = spanBuilder(EXCEPTION_SPAN_ID)
                .addEvents(exceptionEvent(exceptionType("java.lang.RuntimeException")))
                .build();

        ExceptionMetaDataBo bo = mapper.map(id(), span, ROOT_SPAN_ID_LONG, "/api/users/{id}").orElseThrow();

        assertThat(bo.getUriTemplate()).isEqualTo("/api/users/{id}");
    }
}
