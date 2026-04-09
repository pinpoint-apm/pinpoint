package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionWrapperBo;
import com.navercorp.pinpoint.common.server.bo.exception.StackTraceElementWrapperBo;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceExceptionMapperTest {

    private OtlpTraceExceptionMapper mapper;
    private IdAndName idAndName;

    // 16-byte traceId / 8-byte spanId
    private static final ByteString TRACE_ID = ByteString.copyFrom(new byte[16]);
    private static final ByteString SPAN_ID  = ByteString.copyFrom(new byte[]{0,0,0,0,0,0,0,1});

    private static final String STACKTRACE =
            "java.net.UnknownHostException: php.backend.com\n" +
            "\tat java.net.Inet4AddressImpl.lookupAllHostAddr(Native Method)\n" +
            "\tat java.net.InetAddress.getAddressFromNameService(InetAddress.java:1312)\n" +
            "\tat com.example.Client.call(Client.java:42)\n";

    @BeforeEach
    void setUp() {
        mapper = new OtlpTraceExceptionMapper();
        idAndName = new IdAndName("agent-1", null, "my-app");
    }

    // -----------------------------------------------------------------------
    // span with no events → empty
    // -----------------------------------------------------------------------

    @Test
    void noEvents_returnsEmpty() {
        Span span = Span.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .build();

        assertThat(mapper.map(idAndName, span)).isEmpty();
    }

    // -----------------------------------------------------------------------
    // span with non-exception event → empty
    // -----------------------------------------------------------------------

    @Test
    void nonExceptionEvent_returnsEmpty() {
        Span span = Span.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .addEvents(Span.Event.newBuilder().setName("log").build())
                .build();

        assertThat(mapper.map(idAndName, span)).isEmpty();
    }

    // -----------------------------------------------------------------------
    // exception event with no exception.type and no error.type → empty
    // -----------------------------------------------------------------------

    @Test
    void exceptionEventWithNoClassName_returnsEmpty() {
        Span.Event event = Span.Event.newBuilder()
                .setName("exception")
                .addAttributes(kv("exception.message", strVal("some error")))
                .build();

        Span span = Span.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .addEvents(event)
                .build();

        assertThat(mapper.map(idAndName, span)).isEmpty();
    }

    // -----------------------------------------------------------------------
    // happy path: exception.type + message + stacktrace
    // -----------------------------------------------------------------------

    @Test
    void fullExceptionEvent_mapsCorrectly() {
        Span.Event event = Span.Event.newBuilder()
                .setName("exception")
                .setTimeUnixNano(TimeUnit.MILLISECONDS.toNanos(1000L))
                .addAttributes(kv("exception.type",       strVal("java.net.UnknownHostException")))
                .addAttributes(kv("exception.message",    strVal("php.backend.com")))
                .addAttributes(kv("exception.stacktrace", strVal(STACKTRACE)))
                .build();

        Span span = Span.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .setKind(Span.SpanKind.SPAN_KIND_CLIENT)
                .setStartTimeUnixNano(TimeUnit.MILLISECONDS.toNanos(500L))
                .addEvents(event)
                .setStatus(Status.newBuilder().setCode(Status.StatusCode.STATUS_CODE_ERROR).build())
                .build();

        Optional<ExceptionMetaDataBo> result = mapper.map(idAndName, span);
        assertThat(result).isPresent();

        ExceptionMetaDataBo bo = result.get();
        assertThat(bo.getApplicationName()).isEqualTo("my-app");
        assertThat(bo.getAgentId()).isEqualTo("agent-1");

        // transactionId should be bare hex (no ^ delimiters) for OTel round-trip
        String txId = bo.getTransactionId().getId();
        assertThat(txId).doesNotContain("^");
        assertThat(txId).hasSize(32); // 16 bytes → 32 hex chars

        List<ExceptionWrapperBo> wrappers = bo.getExceptionWrapperBos();
        assertThat(wrappers).hasSize(1);

        ExceptionWrapperBo wrapper = wrappers.get(0);
        assertThat(wrapper.getExceptionClassName()).isEqualTo("java.net.UnknownHostException");
        assertThat(wrapper.getExceptionMessage()).isEqualTo("php.backend.com");
        assertThat(wrapper.getStartTime()).isEqualTo(1000L);
        assertThat(wrapper.getExceptionId()).isEqualTo(0); // first event, index=0
        assertThat(wrapper.getExceptionDepth()).isEqualTo(0);

        List<StackTraceElementWrapperBo> frames = wrapper.getStackTraceElements();
        assertThat(frames).hasSize(3);
        assertThat(frames.get(0).getClassName()).isEqualTo("java.net.Inet4AddressImpl");
        assertThat(frames.get(0).getMethodName()).isEqualTo("lookupAllHostAddr");
        assertThat(frames.get(0).getFileName()).isEqualTo("Native Method");
        assertThat(frames.get(0).getLineNumber()).isEqualTo(-2);

        assertThat(frames.get(1).getClassName()).isEqualTo("java.net.InetAddress");
        assertThat(frames.get(1).getMethodName()).isEqualTo("getAddressFromNameService");
        assertThat(frames.get(1).getFileName()).isEqualTo("InetAddress.java");
        assertThat(frames.get(1).getLineNumber()).isEqualTo(1312);

        assertThat(frames.get(2).getClassName()).isEqualTo("com.example.Client");
        assertThat(frames.get(2).getMethodName()).isEqualTo("call");
        assertThat(frames.get(2).getFileName()).isEqualTo("Client.java");
        assertThat(frames.get(2).getLineNumber()).isEqualTo(42);
    }

    // -----------------------------------------------------------------------
    // fallback: no exception.type → use span attribute error.type
    // -----------------------------------------------------------------------

    @Test
    void fallbackToErrorType_whenExceptionTypeAbsent() {
        Span.Event event = Span.Event.newBuilder()
                .setName("exception")
                .addAttributes(kv("exception.message", strVal("connection refused")))
                .build();

        Span span = Span.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .addAttributes(kv("error.type", strVal("java.net.ConnectException")))
                .addEvents(event)
                .build();

        Optional<ExceptionMetaDataBo> result = mapper.map(idAndName, span);
        assertThat(result).isPresent();

        ExceptionWrapperBo wrapper = result.get().getExceptionWrapperBos().get(0);
        assertThat(wrapper.getExceptionClassName()).isEqualTo("java.net.ConnectException");
        assertThat(wrapper.getExceptionMessage()).isEqualTo("connection refused");
    }

    // -----------------------------------------------------------------------
    // startTime fallback: event time=0 → span startTime
    // -----------------------------------------------------------------------

    @Test
    void startTime_fallsBackToSpanStartTime_whenEventTimeIsZero() {
        Span.Event event = Span.Event.newBuilder()
                .setName("exception")
                // timeUnixNano not set → defaults to 0
                .addAttributes(kv("exception.type", strVal("java.lang.RuntimeException")))
                .build();

        long spanStartNano = TimeUnit.MILLISECONDS.toNanos(2000L);
        Span span = Span.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .setStartTimeUnixNano(spanStartNano)
                .addEvents(event)
                .build();

        Optional<ExceptionMetaDataBo> result = mapper.map(idAndName, span);
        assertThat(result).isPresent();

        long startTime = result.get().getExceptionWrapperBos().get(0).getStartTime();
        assertThat(startTime).isEqualTo(2000L);
    }

    // -----------------------------------------------------------------------
    // multiple exception events in one span → multiple wrappers
    // -----------------------------------------------------------------------

    @Test
    void multipleExceptionEvents_producesMultipleWrappers() {
        Span.Event event1 = Span.Event.newBuilder()
                .setName("exception")
                .setTimeUnixNano(TimeUnit.MILLISECONDS.toNanos(100L))
                .addAttributes(kv("exception.type", strVal("java.io.IOException")))
                .build();

        Span.Event event2 = Span.Event.newBuilder()
                .setName("exception")
                .setTimeUnixNano(TimeUnit.MILLISECONDS.toNanos(200L))
                .addAttributes(kv("exception.type", strVal("java.lang.RuntimeException")))
                .build();

        Span span = Span.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .addEvents(event1)
                .addEvents(event2)
                .build();

        Optional<ExceptionMetaDataBo> result = mapper.map(idAndName, span);
        assertThat(result).isPresent();

        List<ExceptionWrapperBo> wrappers = result.get().getExceptionWrapperBos();
        assertThat(wrappers).hasSize(2);
        assertThat(wrappers.get(0).getExceptionId()).isEqualTo(0);
        assertThat(wrappers.get(0).getExceptionClassName()).isEqualTo("java.io.IOException");
        assertThat(wrappers.get(1).getExceptionId()).isEqualTo(1);
        assertThat(wrappers.get(1).getExceptionClassName()).isEqualTo("java.lang.RuntimeException");
    }

    // -----------------------------------------------------------------------
    // parseStackTrace edge cases
    // -----------------------------------------------------------------------

    @Test
    void parseStackTrace_nullOrEmpty_returnsEmptyList() {
        assertThat(OtlpTraceExceptionMapper.parseStackTrace(null)).isEmpty();
        assertThat(OtlpTraceExceptionMapper.parseStackTrace("")).isEmpty();
    }

    @Test
    void parseStackTrace_headerLineSkipped() {
        String raw = "java.lang.RuntimeException: boom\n" +
                     "\tat com.example.Foo.bar(Foo.java:10)\n";

        List<StackTraceElementWrapperBo> frames = OtlpTraceExceptionMapper.parseStackTrace(raw);
        assertThat(frames).hasSize(1);
        assertThat(frames.get(0).getClassName()).isEqualTo("com.example.Foo");
        assertThat(frames.get(0).getMethodName()).isEqualTo("bar");
        assertThat(frames.get(0).getFileName()).isEqualTo("Foo.java");
        assertThat(frames.get(0).getLineNumber()).isEqualTo(10);
    }

    @Test
    void parseStackTrace_unknownSource() {
        String raw = "\tat com.example.Foo.bar(Unknown Source)\n";

        List<StackTraceElementWrapperBo> frames = OtlpTraceExceptionMapper.parseStackTrace(raw);
        assertThat(frames).hasSize(1);
        assertThat(frames.get(0).getFileName()).isEqualTo("Unknown Source");
        assertThat(frames.get(0).getLineNumber()).isEqualTo(-1);
    }

    @Test
    void parseStackTrace_nativeMethod() {
        String raw = "\tat java.net.Inet4AddressImpl.lookupAllHostAddr(Native Method)\n";

        List<StackTraceElementWrapperBo> frames = OtlpTraceExceptionMapper.parseStackTrace(raw);
        assertThat(frames).hasSize(1);
        assertThat(frames.get(0).getFileName()).isEqualTo("Native Method");
        assertThat(frames.get(0).getLineNumber()).isEqualTo(-2);
    }

    @Test
    void parseStackTrace_innerClass() {
        String raw = "\tat com.example.Outer$Inner.method(Outer.java:99)\n";

        List<StackTraceElementWrapperBo> frames = OtlpTraceExceptionMapper.parseStackTrace(raw);
        assertThat(frames).hasSize(1);
        assertThat(frames.get(0).getClassName()).isEqualTo("com.example.Outer$Inner");
        assertThat(frames.get(0).getMethodName()).isEqualTo("method");
        assertThat(frames.get(0).getLineNumber()).isEqualTo(99);
    }

    // -----------------------------------------------------------------------
    // uriTemplate resolution order
    // -----------------------------------------------------------------------

    @Test
    void uriTemplate_prefersUrlPath() {
        List<KeyValue> attrs = List.of(
                kv("url.path", strVal("/api/v1")),
                kv("http.url", strVal("http://host/api/v2")),
                kv("http.target", strVal("/api/v3"))
        );
        Span.Event event = Span.Event.newBuilder()
                .setName("exception")
                .addAttributes(kv("exception.type", strVal("java.lang.Exception")))
                .build();

        Span span = Span.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .setName("fallback-name")
                .addAllAttributes(attrs)
                .addEvents(event)
                .build();

        Optional<ExceptionMetaDataBo> result = mapper.map(idAndName, span);
        assertThat(result).isPresent();
        assertThat(result.get().getUriTemplate()).isEqualTo("/api/v1");
    }

    @Test
    void uriTemplate_fallsBackToSpanName_whenNoHttpAttrs() {
        Span.Event event = Span.Event.newBuilder()
                .setName("exception")
                .addAttributes(kv("exception.type", strVal("java.lang.Exception")))
                .build();

        Span span = Span.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .setName("my-operation")
                .addEvents(event)
                .build();

        Optional<ExceptionMetaDataBo> result = mapper.map(idAndName, span);
        assertThat(result).isPresent();
        assertThat(result.get().getUriTemplate()).isEqualTo("my-operation");
    }
}
