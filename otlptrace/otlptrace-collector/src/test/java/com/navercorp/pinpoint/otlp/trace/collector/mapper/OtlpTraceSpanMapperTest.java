package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceSpanMapperTest {

    // =======================================================================
    // extractHostAndPort
    // =======================================================================

    @Test
    void extractHostAndPort_withSchemeAndPort() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com:8080/path"))
                .isEqualTo("example.com:8080");
    }

    @Test
    void extractHostAndPort_withSchemeNoPort() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("https://example.com/path"))
                .isEqualTo("example.com");
    }

    @Test
    void extractHostAndPort_withSchemeNoPath() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com:9090"))
                .isEqualTo("example.com:9090");
    }

    @Test
    void extractHostAndPort_noScheme() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("example.com:8080/path"))
                .isEqualTo("example.com:8080");
    }

    @Test
    void extractHostAndPort_withQuery() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com/path?key=val"))
                .isEqualTo("example.com");
    }

    @Test
    void extractHostAndPort_withFragment() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com/path#section"))
                .isEqualTo("example.com");
    }

    @Test
    void extractHostAndPort_null() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort(null)).isNull();
    }

    @Test
    void extractHostAndPort_empty() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("")).isNull();
    }

    // =======================================================================
    // extractPath
    // =======================================================================

    @Test
    void extractPath_withSchemeAndPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/api/users"))
                .isEqualTo("/api/users");
    }

    @Test
    void extractPath_withQuery() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/api?key=val"))
                .isEqualTo("/api");
    }

    @Test
    void extractPath_withFragment() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/api#section"))
                .isEqualTo("/api");
    }

    @Test
    void extractPath_noPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com"))
                .isEqualTo("/");
    }

    @Test
    void extractPath_rootPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/"))
                .isEqualTo("/");
    }

    @Test
    void extractPath_null() {
        assertThat(OtlpTraceSpanMapper.extractPath(null)).isEqualTo("/");
    }

    @Test
    void extractPath_empty() {
        assertThat(OtlpTraceSpanMapper.extractPath("")).isEqualTo("/");
    }

    @Test
    void extractPath_noScheme() {
        assertThat(OtlpTraceSpanMapper.extractPath("example.com/api/hello"))
                .isEqualTo("/api/hello");
    }

    @Test
    void extractPath_deepPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("https://example.com:443/a/b/c/d"))
                .isEqualTo("/a/b/c/d");
    }

    // =======================================================================
    // resolveExceptionClass
    // =======================================================================

    @Test
    void resolveExceptionClass_fromExceptionEvent() {
        Span span = Span.newBuilder()
                .addEvents(Span.Event.newBuilder()
                        .setName("exception")
                        .addAttributes(kv("exception.type", strVal("java.net.UnknownHostException")))
                        .build())
                .build();

        assertThat(OtlpTraceSpanMapper.resolveExceptionClass(span, Map.of()))
                .isEqualTo("java.net.UnknownHostException");
    }

    @Test
    void resolveExceptionClass_fallsBackToErrorTypeAttribute() {
        Span span = Span.newBuilder().build();
        Map<String, Object> attributes = Map.of("error.type", "java.io.IOException");

        assertThat(OtlpTraceSpanMapper.resolveExceptionClass(span, attributes))
                .isEqualTo("java.io.IOException");
    }

    @Test
    void resolveExceptionClass_eventTypeTakesPrecedenceOverAttribute() {
        Span span = Span.newBuilder()
                .addEvents(Span.Event.newBuilder()
                        .setName("exception")
                        .addAttributes(kv("exception.type", strVal("java.net.UnknownHostException")))
                        .build())
                .build();
        Map<String, Object> attributes = Map.of("error.type", "java.io.IOException");

        assertThat(OtlpTraceSpanMapper.resolveExceptionClass(span, attributes))
                .isEqualTo("java.net.UnknownHostException");
    }

    @Test
    void resolveExceptionClass_noEventNoAttribute_returnsNull() {
        Span span = Span.newBuilder().build();

        assertThat(OtlpTraceSpanMapper.resolveExceptionClass(span, Map.of())).isNull();
    }

    @Test
    void resolveExceptionClass_nonExceptionEventSkipped() {
        Span span = Span.newBuilder()
                .addEvents(Span.Event.newBuilder().setName("log").build())
                .build();
        Map<String, Object> attributes = Map.of("error.type", "java.io.IOException");

        assertThat(OtlpTraceSpanMapper.resolveExceptionClass(span, attributes))
                .isEqualTo("java.io.IOException");
    }

    // =======================================================================
    // resolveExceptionMessage
    // =======================================================================

    @Test
    void resolveExceptionMessage_fromExceptionEventMessage() {
        Span span = Span.newBuilder()
                .setStatus(Status.newBuilder().setMessage("status msg").build())
                .addEvents(Span.Event.newBuilder()
                        .setName("exception")
                        .addAttributes(kv("exception.message", strVal("host not found")))
                        .build())
                .build();

        assertThat(OtlpTraceSpanMapper.resolveExceptionMessage(span, "SomeClass"))
                .isEqualTo("host not found");
    }

    @Test
    void resolveExceptionMessage_fallsBackToStatusMessage() {
        Span span = Span.newBuilder()
                .setStatus(Status.newBuilder().setMessage("status msg").build())
                .build();

        assertThat(OtlpTraceSpanMapper.resolveExceptionMessage(span, "SomeClass"))
                .isEqualTo("status msg");
    }

    @Test
    void resolveExceptionMessage_fallsBackToExceptionClass() {
        Span span = Span.newBuilder().build();

        assertThat(OtlpTraceSpanMapper.resolveExceptionMessage(span, "java.io.IOException"))
                .isEqualTo("java.io.IOException");
    }

    @Test
    void resolveExceptionMessage_eventMessageTakesPrecedenceOverStatus() {
        Span span = Span.newBuilder()
                .setStatus(Status.newBuilder().setMessage("status msg").build())
                .addEvents(Span.Event.newBuilder()
                        .setName("exception")
                        .addAttributes(kv("exception.message", strVal("event msg")))
                        .build())
                .build();

        assertThat(OtlpTraceSpanMapper.resolveExceptionMessage(span, "SomeClass"))
                .isEqualTo("event msg");
    }

}