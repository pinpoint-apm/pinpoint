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

package com.navercorp.pinpoint.otlp.log.collector.mapper;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionWrapperBo;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.IdAndName;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpExceptionMapper;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.logs.v1.LogRecord;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.SPAN_ID;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.TRACE_ID;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.exceptionRecord;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.kv;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.record;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtlpLogExceptionMapperTest {

    private static final IdAndName ID = new IdAndName("agent-1", "agent-1", "app-1", "svc");
    private static final long NOW = 1_700_000_000_000L;
    private static final int MESSAGE_MAX_BYTES = 64;

    private static final String JAVA_STACK = "java.lang.IllegalStateException: boom\n"
            + "\tat com.example.App.run(App.java:10)\n"
            + "\tat com.example.Main.main(Main.java:5)\n";

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OtlpExceptionMapper traceMapper = new OtlpExceptionMapper(2048, 256, 2048, registry);
    private final OtlpLogExceptionMapper mapper = new OtlpLogExceptionMapper(traceMapper, MESSAGE_MAX_BYTES, registry, () -> NOW);

    private static ExceptionLogCandidate candidate(LogRecord record) {
        return candidate(record, false);
    }

    private static ExceptionLogCandidate candidate(LogRecord record, boolean unsampled) {
        return new ExceptionLogCandidate(ID, "java", record, unsampled);
    }

    @Test
    void mapsJavaAgentRecord_spanIdIsTheRecordsOwnSpan() {
        LogRecord record = exceptionRecord(SPAN_ID, "java.lang.IllegalStateException", "boom", JAVA_STACK)
                .setTimeUnixNano(2_000_000_000L) // 2000 ms
                .addAttributes(kv("http.route", "/users/{id}"))
                .build();

        Optional<ExceptionMetaDataBo> result = mapper.map(candidate(record));

        assertThat(result).isPresent();
        ExceptionMetaDataBo bo = result.get();
        long ownSpanId = OtlpTraceMapperUtils.getSpanId(ByteString.copyFrom(SPAN_ID));
        // Unlike the span-event path (root span id), the log path stores the exception-bearing span.
        assertThat(bo.getSpanId()).isEqualTo(ownSpanId);
        assertThat(bo.getTransactionId()).isEqualTo(new OtelServerTraceId(TRACE_ID));
        assertThat(bo.getServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
        assertThat(bo.getServiceName()).isEqualTo("svc");
        assertThat(bo.getApplicationName()).isEqualTo("app-1");
        assertThat(bo.getAgentId()).isEqualTo("agent-1");
        assertThat(bo.getUriTemplate()).isEqualTo("/users/{id}");

        assertThat(bo.getExceptionWrapperBos()).hasSize(1);
        ExceptionWrapperBo wrapper = bo.getExceptionWrapperBos().get(0);
        assertThat(wrapper.getExceptionClassName()).isEqualTo("java.lang.IllegalStateException");
        assertThat(wrapper.getExceptionMessage()).isEqualTo("boom");
        assertThat(wrapper.getStartTime()).isEqualTo(2000L);
        // exceptionId = the same span id, matching the trace path's discriminator rule.
        assertThat(wrapper.getExceptionId()).isEqualTo(ownSpanId);
        assertThat(wrapper.getExceptionDepth()).isZero();
        assertThat(wrapper.getStackTraceElements()).hasSize(2);
        assertThat(wrapper.getStackTraceElements().get(0).getClassName()).isEqualTo("com.example.App");
        assertThat(wrapper.getStackTraceElements().get(0).getMethodName()).isEqualTo("run");
        assertThat(wrapper.getStackTraceElements().get(0).getLineNumber()).isEqualTo(10);
    }

    @Test
    void noExceptionType_andNoErrorType_isEmpty() {
        LogRecord record = record(SPAN_ID, kv("exception.message", "boom"), kv("exception.stacktrace", JAVA_STACK)).build();

        assertThat(mapper.map(candidate(record))).isEmpty();
    }

    @Test
    void errorType_isTheFallbackForExceptionType() {
        LogRecord record = record(SPAN_ID, kv("exception.message", "boom"), kv("error.type", "java.io.IOException")).build();

        Optional<ExceptionMetaDataBo> result = mapper.map(candidate(record));

        assertThat(result).isPresent();
        assertThat(result.get().getExceptionWrapperBos().get(0).getExceptionClassName()).isEqualTo("java.io.IOException");
    }

    @Test
    void goStyleRecord_messageOnly_noStackTrace_isStoredWithEmptyStack() {
        LogRecord record = record(SPAN_ID, kv("exception.type", "*errors.errorString"), kv("exception.message", "dial tcp: refused")).build();

        ExceptionMetaDataBo bo = mapper.map(new ExceptionLogCandidate(ID, "go", record, false)).orElseThrow();

        ExceptionWrapperBo wrapper = bo.getExceptionWrapperBos().get(0);
        assertThat(wrapper.getExceptionClassName()).isEqualTo("*errors.errorString");
        assertThat(wrapper.getExceptionMessage()).isEqualTo("dial tcp: refused");
        assertThat(wrapper.getStackTraceElements()).isEmpty();
    }

    @Test
    void message_fallsBackToStringBody_thenEmpty() {
        LogRecord withBody = record(SPAN_ID, kv("exception.type", "T"))
                .setBody(AnyValue.newBuilder().setStringValue("Request failed"))
                .build();
        LogRecord kvlistBody = record(SPAN_ID, kv("exception.type", "T"))
                .setBody(kv("k", "v").getValue().toBuilder().clearStringValue()
                        .setKvlistValue(io.opentelemetry.proto.common.v1.KeyValueList.newBuilder().addValues(kv("k", "v"))))
                .build();

        assertThat(mapper.map(candidate(withBody)).orElseThrow().getExceptionWrapperBos().get(0).getExceptionMessage())
                .isEqualTo("Request failed");
        assertThat(mapper.map(candidate(kvlistBody)).orElseThrow().getExceptionWrapperBos().get(0).getExceptionMessage())
                .isEmpty();
    }

    @Test
    void time_fallsBack_timeUnixNano_observedTimeUnixNano_receiveTime() {
        LogRecord timed = exceptionRecord(SPAN_ID, "T", "m", null).setTimeUnixNano(3_000_000_000L).setObservedTimeUnixNano(4_000_000_000L).build();
        LogRecord observedOnly = exceptionRecord(SPAN_ID, "T", "m", null).setObservedTimeUnixNano(4_000_000_000L).build();
        LogRecord none = exceptionRecord(SPAN_ID, "T", "m", null).build();

        assertThat(startTime(timed)).isEqualTo(3000L);
        assertThat(startTime(observedOnly)).isEqualTo(4000L);
        assertThat(startTime(none)).isEqualTo(NOW);
    }

    private long startTime(LogRecord record) {
        return mapper.map(candidate(record)).orElseThrow().getExceptionWrapperBos().get(0).getStartTime();
    }

    @Test
    void uriTemplate_emptyWhenHttpRouteAbsentOrBlank_neverNullNorARawPath() {
        // A null would reach Pinot as its STRING null sentinel and render as the literal "null" in
        // Error Analysis; the trace path and the native agent store "" for "no route".
        LogRecord none = exceptionRecord(SPAN_ID, "T", "m", null).addAttributes(kv("url.path", "/users/42")).build();
        LogRecord blank = exceptionRecord(SPAN_ID, "T", "m", null).addAttributes(kv("http.route", "")).build();

        assertThat(mapper.map(candidate(none)).orElseThrow().getUriTemplate()).isEmpty();
        assertThat(mapper.map(candidate(blank)).orElseThrow().getUriTemplate()).isEmpty();
    }

    @Test
    void message_isCappedInUtf8Bytes_andCounted() {
        String longMessage = "x".repeat(MESSAGE_MAX_BYTES + 10);
        LogRecord record = exceptionRecord(SPAN_ID, "T", longMessage, null).build();

        String stored = mapper.map(candidate(record)).orElseThrow().getExceptionWrapperBos().get(0).getExceptionMessage();

        assertThat(stored.getBytes(StandardCharsets.UTF_8)).hasSize(MESSAGE_MAX_BYTES);
        assertThat(registry.get("collector.otlplog.exception.truncated").tag("field", "message").counter().count()).isEqualTo(1.0);
    }

    @Test
    void message_withinCap_isNotTruncatedOrCounted() {
        LogRecord record = exceptionRecord(SPAN_ID, "T", "short", null).build();

        mapper.map(candidate(record));

        assertThat(registry.get("collector.otlplog.exception.truncated").tag("field", "message").counter().count()).isZero();
    }

    @Test
    void stackTraceParsing_isDelegatedToTheTracePathParser_perSdkLanguage() {
        String pythonStack = "Traceback (most recent call last):\n"
                + "  File \"/app/main.py\", line 12, in handler\n"
                + "    raise ValueError(\"bad\")\n"
                + "ValueError: bad\n";
        LogRecord record = exceptionRecord(SPAN_ID, "ValueError", "bad", pythonStack).build();

        ExceptionMetaDataBo bo = mapper.map(new ExceptionLogCandidate(ID, "python", record, false)).orElseThrow();

        assertThat(bo.getExceptionWrapperBos().get(0).getStackTraceElements()).isNotEmpty();
        assertThat(registry.get("collector.otlptrace.exception.parsed").tag("parser", "python").counter().count()).isEqualTo(1.0);
    }

    @Test
    void messageMaxBytes_mustBePositive() {
        assertThatThrownBy(() -> new OtlpLogExceptionMapper(traceMapper, 0, registry, () -> NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
