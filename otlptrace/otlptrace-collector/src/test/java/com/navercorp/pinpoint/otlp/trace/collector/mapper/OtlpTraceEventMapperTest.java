package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.boolVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.doubleVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.intVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceEventMapperTest {

    private OtlpTraceEventMapper mapper;
    private List<AnnotationBo> annotations;
    private AnnotationWriter writer;

    @BeforeEach
    void setUp() {
        mapper = new OtlpTraceEventMapper(new ObjectMapper());
        annotations = new ArrayList<>();
        writer = annotations::add;
    }

    @Test
    void nameOnly_writesEmptyAttributesObject() {
        Span.Event event = Span.Event.newBuilder()
                .setName("exception")
                .build();

        mapper.addEventToAnnotation(event, writer);

        assertThat(annotations).hasSize(1);
        AnnotationBo bo = annotations.get(0);
        assertThat(bo.getKey()).isEqualTo(AnnotationKey.OPENTELEMETRY_EVENT.getCode());
        assertThat(bo.getValue()).isEqualTo("{\"name\":\"exception\",\"attributes\":{}}");
    }

    @Test
    void withStringAttribute_writesAttributeMap() {
        List<KeyValue> attrs = List.of(
                kv("exception.type", strVal("java.lang.RuntimeException"))
        );
        Span.Event event = Span.Event.newBuilder()
                .setName("exception")
                .addAllAttributes(attrs)
                .build();

        mapper.addEventToAnnotation(event, writer);

        assertThat(annotations).hasSize(1);
        assertThat(annotations.get(0).getValue())
                .isEqualTo("{\"name\":\"exception\",\"attributes\":{\"exception.type\":\"java.lang.RuntimeException\"}}");
    }

    @Test
    void withTimestamp_emitsTimeUnixNano() throws Exception {
        long epochNanos = TimeUnit.MILLISECONDS.toNanos(1_700_000_000_123L);
        Span.Event event = Span.Event.newBuilder()
                .setName("checkpoint")
                .setTimeUnixNano(epochNanos)
                .build();

        mapper.addEventToAnnotation(event, writer);

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        assertThat(root).containsEntry("name", "checkpoint");
        assertThat(root).containsEntry("timeUnixNano", epochNanos);
        assertThat(root.get("attributes")).isEqualTo(Map.of());
    }

    @Test
    void withoutTimestamp_omitsTimeUnixNanoField() throws Exception {
        Span.Event event = Span.Event.newBuilder().setName("notime").build();

        mapper.addEventToAnnotation(event, writer);

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        assertThat(root).doesNotContainKey("timeUnixNano");
    }

    @Test
    void exceptionEvent_stripsStacktraceAttribute() throws Exception {
        List<KeyValue> attrs = List.of(
                kv("exception.type", strVal("java.lang.RuntimeException")),
                kv("exception.message", strVal("boom")),
                kv("exception.stacktrace", strVal("java.lang.RuntimeException: boom\n\tat ..."))
        );
        Span.Event event = Span.Event.newBuilder()
                .setName("exception")
                .addAllAttributes(attrs)
                .build();

        mapper.addEventToAnnotation(event, writer);

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) root.get("attributes");
        assertThat(attributes)
                .containsEntry("exception.type", "java.lang.RuntimeException")
                .containsEntry("exception.message", "boom")
                .doesNotContainKey("exception.stacktrace");
    }

    @Test
    void nonExceptionEvent_keepsAllAttributes() throws Exception {
        List<KeyValue> attrs = List.of(
                kv("exception.stacktrace", strVal("not an exception event"))
        );
        Span.Event event = Span.Event.newBuilder()
                .setName("custom")
                .addAllAttributes(attrs)
                .build();

        mapper.addEventToAnnotation(event, writer);

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) root.get("attributes");
        assertThat(attributes).containsKey("exception.stacktrace");
    }

    @Test
    void withMultipleAttributeTypes_writesCorrectJson() throws Exception {
        List<KeyValue> attrs = List.of(
                kv("count", intVal(42L)),
                kv("flag", boolVal(true)),
                kv("ratio", doubleVal(0.5))
        );
        Span.Event event = Span.Event.newBuilder()
                .setName("my-event")
                .addAllAttributes(attrs)
                .build();

        mapper.addEventToAnnotation(event, writer);

        assertThat(annotations).hasSize(1);
        String json = (String) annotations.get(0).getValue();

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue(json, Map.class);
        assertThat(root).containsEntry("name", "my-event");

        @SuppressWarnings("unchecked")
        Map<String, Object> inner = (Map<String, Object>) root.get("attributes");
        assertThat(inner).containsEntry("count", 42);
        assertThat(inner).containsEntry("flag", true);
        assertThat(inner).containsEntry("ratio", 0.5);
    }

    @Test
    void withArrayAttribute_writesArray() throws Exception {
        ArrayValue arrayValue = ArrayValue.newBuilder()
                .addValues(strVal("a"))
                .addValues(strVal("b"))
                .build();
        Span.Event event = Span.Event.newBuilder()
                .setName("tagged")
                .addAttributes(kv("tags", AnyValue.newBuilder().setArrayValue(arrayValue).build()))
                .build();

        mapper.addEventToAnnotation(event, writer);

        assertThat(annotations).hasSize(1);
        String json = (String) annotations.get(0).getValue();

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue(json, Map.class);
        assertThat(root).containsEntry("name", "tagged");

        @SuppressWarnings("unchecked")
        Map<String, Object> inner = (Map<String, Object>) root.get("attributes");
        assertThat(inner.get("tags")).isInstanceOf(List.class)
                .asList().containsExactly("a", "b");
    }

    @Test
    void annotationKey_isOpentelemetryEvent() {
        Span.Event event = Span.Event.newBuilder().setName("check").build();

        mapper.addEventToAnnotation(event, writer);

        assertThat(annotations.get(0).getKey())
                .isEqualTo(AnnotationKey.OPENTELEMETRY_EVENT.getCode());
    }

}
